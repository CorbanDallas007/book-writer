#include "whisper.h"
#include "whisper-arch.h"

#include "ggml.h"
#include "ggml-cpp.h"
#include "ggml-alloc.h"
#include "ggml-backend.h"

#ifdef WHISPER_USE_COREML
#include "coreml/whisper-encoder.h"
#endif

#ifdef WHISPER_USE_OPENVINO
#include "openvino/whisper-openvino-encoder.h"
#endif

#include <atomic>
#include <algorithm>
#include <cassert>
#include <cfloat>
#define _USE_MATH_DEFINES
#include <cmath>
#include <climits>
#include <cstdarg>
#include <cstdio>
#include <cstring>
#include <fstream>
#include <functional>
#include <map>
#include <random>
#include <regex>
#include <set>
#include <string>
#include <thread>
#include <vector>

#ifdef _MSC_VER
#include <codecvt>
#endif

#if defined(WHISPER_BIG_ENDIAN)
template<typename T>
static T byteswap(T value) {
    T value_swapped;
    char * source = reinterpret_cast<char *>(&value);
    char * target = reinterpret_cast<char *>(&value_swapped);
    int size = sizeof(T);
    for (int i = 0; i < size; i++) {
        target[size - 1 - i] = source[i];
    }
    return value_swapped;
}

template<typename T>
static void byteswap_tensor_data(ggml_tensor * tensor) {
    T * datum = reinterpret_cast<T *>(tensor->data);
    for (int i = 0; i < ggml_nelements(tensor); i++) {
        datum[i] = byteswap(datum[i]);
    }
}

static void byteswap_tensor(ggml_tensor * tensor) {
    switch (tensor->type) {
        case GGML_TYPE_I16: {
            byteswap_tensor_data<int16_t>(tensor);
            break;
        }
        case GGML_TYPE_F16: {
            byteswap_tensor_data<ggml_fp16_t>(tensor);
            break;
        }
        case GGML_TYPE_I32: {
            byteswap_tensor_data<int32_t>(tensor);
            break;
        }
        case GGML_TYPE_F32: {
            byteswap_tensor_data<float>(tensor);
            break;
        }
        default: { // GML_TYPE_I8
            break;
        }
    }
}

#define BYTESWAP_VALUE(d) d = byteswap(d)
#define BYTESWAP_FILTERS(f)           \
    do {                              \
        for (auto & datum : f.data) { \
            datum = byteswap(datum);  \
        }                             \
    } while (0)
#define BYTESWAP_TENSOR(t)  \
    do {                    \
        byteswap_tensor(t); \
    } while (0)
#else
#define BYTESWAP_VALUE(d) do {} while (0)
#define BYTESWAP_FILTERS(f) do {} while (0)
#define BYTESWAP_TENSOR(t) do {} while (0)
#endif

#ifdef __GNUC__
#ifdef __MINGW32__
#define WHISPER_ATTRIBUTE_FORMAT(...) __attribute__((format(gnu_printf, __VA_ARGS__)))
#else
#define WHISPER_ATTRIBUTE_FORMAT(...) __attribute__((format(printf, __VA_ARGS__)))
#endif
#else
#define WHISPER_ATTRIBUTE_FORMAT(...)
#endif

// logging

WHISPER_ATTRIBUTE_FORMAT(2, 3)
static void whisper_log_internal(ggml_log_level level, const char * format, ...);
static void whisper_log_callback_default(ggml_log_level level, const char * text, void * user_data);

#define WHISPER_LOG_ERROR(...) whisper_log_internal(GGML_LOG_LEVEL_ERROR, __VA_ARGS__)
#define WHISPER_LOG_WARN(...)  whisper_log_internal(GGML_LOG_LEVEL_WARN , __VA_ARGS__)
#define WHISPER_LOG_INFO(...)  whisper_log_internal(GGML_LOG_LEVEL_INFO , __VA_ARGS__)

#if defined(WHISPER_DEBUG)
#define WHISPER_LOG_DEBUG(...) whisper_log_internal(GGML_LOG_LEVEL_DEBUG, __VA_ARGS__)
#else
#define WHISPER_LOG_DEBUG(...)
#endif

#define WHISPER_ASSERT(x) \
    do { \
        if (!(x)) { \
            WHISPER_LOG_ERROR("WHISPER_ASSERT: %s:%d: %s\n", __FILE__, __LINE__, #x); \
            abort(); \
        } \
    } while (0)

#define WHISPER_MAX_DECODERS 8

// temperature below which we condition on past text history
static constexpr float WHISPER_HISTORY_CONDITIONING_TEMP_CUTOFF = 0.5f;

#define WHISPER_MAX_NODES 4096

// ...
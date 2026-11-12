#pragma once

#include <stdint.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

// Whisper sampling strategy
typedef enum {
    WHISPER_SAMPLING_GREEDY = 0,
    WHISPER_SAMPLING_BEAM_SEARCH = 1,
} whisper_sampling_strategy;

// Forward declaration of whisper_context
struct whisper_context;

// Whisper full processing parameters
struct whisper_full_params {
    whisper_sampling_strategy strategy;
    int n_max_text_ctx;
    int n_max_segments;
    int n_threads;
    int n_processors;
    int offset_ms;
    int max_len;
    bool speed_up;
    bool translate;
    bool no_context;
    bool no_timestamps;
    bool print_special;
    bool print_progress;
    bool print_realtime;
    bool print_timestamps;
    bool token_timestamps;
    bool vad;
    float temperature;
    float max_initial_ts;
    float length_penalty;
    float temperature_inc;
    float entropy_thold;
    float logprob_thold;
    float no_speech_thold;
    int32_t * initial_prompt;
    int32_t n_initial_prompt;
    const char * language;
    const char * suppress_blank;
    const char * suppress_non_speech_tokens;
    void * new_segment_callback;
    void * new_segment_callback_user_data;
    void * encoder_begin_callback;
    void * encoder_begin_callback_user_data;
};

// Initialize Whisper context from model file
struct whisper_context * whisper_init_from_file(const char * path_model);

// Free Whisper context
void whisper_free(struct whisper_context * ctx);

// Default parameters for whisper_full
struct whisper_full_params whisper_full_default_params(whisper_sampling_strategy strategy);

// Run Whisper speech recognition
// Returns number of tokens on success, negative on error
int whisper_full(struct whisper_context * ctx, struct whisper_full_params params, const float * samples, int n_samples);

// Get recognized text for a segment
const char * whisper_full_get_segment_text(struct whisper_context * ctx, int segment_index);

// Get number of segments
int whisper_full_n_segments(struct whisper_context * ctx);

// Get number of tokens
int whisper_full_n_tokens(struct whisper_context * ctx);

#ifdef __cplusplus
}
#endif

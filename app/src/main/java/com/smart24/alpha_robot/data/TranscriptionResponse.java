//package com.smart24.alpha_robot.data;
//
//import com.google.gson.annotations.SerializedName;
//
//import java.util.List;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Setter
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//public class TranscriptionResponse {
//    private String text;
//    private String task;
//    private String language;
//    private Double duration;
//    private List<Word> words;
//    private List<Segment> segments;
//
//    @Setter
//    @Getter
//    public class Word {
//        private String word;
//        private Double start;
//        private Double end;
//    }
//
//    @Setter
//    @Getter
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public class Segment {
//        private int id;
//        private int seek;
//        private Double start;
//        private Double end;
//        private String text;
//        private List<Integer> tokens;
//        private Double temperature;
//        @SerializedName("avg_logprob")
//        private Double avgLogprob;
//        @SerializedName("compression_ratio")
//        private Double compressionRatio;
//        @SerializedName("no_speech_prob")
//        private Double noSpeechProb;
//        private List<Word> words;
//    }
//
//}

package com.example.lancamentoapi.configuration.property;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "launchs")
public class ApiProperty {

    @Setter
    @Getter
//    private String originPermitted = "http://localhost:8000";
    private String originPermitted;

    @Getter
    private final Security security = new Security();

    @Getter
    private final Mail mail = new Mail();

    @Getter
    private final S3 s3 = new S3();

    @Setter
    @Getter
    public static class Security {

        private boolean enableHttps;
        private String frontEndClient;
        private String frontEndPassword;
        private String mobileClient;
        private String mobilePassword;
    }

    @Setter
    @Getter
    public static class Mail {

        private String host;
        private Integer port;
        private String username;
        private String password;
    }

    @Setter
    @Getter
    public static class S3 {
        private String accessKeyId;

        private String secretAccessKey;

        private String bucket;
    }
}

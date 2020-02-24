package file.manager.lln.infrastructure;


@SuppressWarnings("unused")
public final class Constants {

    enum LogLevel {
        TRACE(5), DEBUG(4), WARNING(3), ERROR(2), FATAL(1), NONE(0);

        private final int logLevelId;

        LogLevel(int logLevelId) {
            this.logLevelId = logLevelId;
        }

        public int getValue() {
            return this.logLevelId;
        }
    }


    public static final class Protocol {

        public static final String HTTP = "http";
        public static final String HTTPS = "https";
        public static final String CA_TLS = "TLS";

    }

    public static final class LogTag {

        public static final String DEFAULT_LOG_TAG = "FileManager";
        public static final String UNIT_TEST_LOG_TAG = "unittest";

    }

}

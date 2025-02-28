package emu.grasscutter.config;

import ch.qos.logback.classic.Level;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.utils.*;
import lombok.NoArgsConstructor;

import java.util.*;

import static emu.grasscutter.Grasscutter.*;

/**
 * *when your JVM fails*
 */
public class ConfigContainer {
    /*
     * Configuration changes:
     * Version  5 - 'questing' has been changed from a boolean
     *              to a container of options ('questOptions').
     *              This field will be removed in future versions.
     * Version  6 - 'questing' has been fully replaced with 'questOptions'.
     *              The field for 'legacyResources' has been removed.
     * Version  7 - 'regionKey' is being added for authentication
     *              with the new dispatch server.
     * Version  8 - 'server' is being added for enforcing handbook server
     *              addresses.
     * Version  9 - 'limits' was added for handbook requests.
     * Version 10 - 'trialCostumes' was added for enabling costumes
     *              on trial avatars.
     * Version 11 - 'server.fastRequire' was added for disabling the new
     *              Lua script require system if performance is a concern.
     * Version 12 - 'http.startImmediately' was added to control whether the
     *              HTTP server should start immediately.
     * Version 13 - 'game.useUniquePacketKey' was added to control whether the
     *              encryption key used for packets is a constant or randomly generated.
     * Version 14 - 'dispatch.regions' 'dispatch.resources' was added to
     *              support download resources.
     */
    private static int version() {
        return 14;
    }

    /**
     * Attempts to update the server's existing configuration.
     */
    public static void updateConfig() {
        try { // Check if the server is using a legacy config.
            var configObject = JsonUtils.loadToClass(Grasscutter.configFile.toPath(), JsonObject.class);
            if (!configObject.has("version")) {
                Grasscutter.getLogger().info("Updating legacy config...");
                Grasscutter.saveConfig(null);
            }
        } catch (Exception ignored) {
        }

        var existing = config.version;
        var latest = version();

        if (existing == latest)
            return;

        // Create a new configuration instance.
        var updated = new ConfigContainer();
        // Update all configuration fields.
        var fields = ConfigContainer.class.getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            try {
                field.set(updated, field.get(config));
            } catch (Exception exception) {
                Grasscutter.getLogger().error("Failed to update a configuration field.", exception);
            }
        });
        updated.version = version();

        try { // Save configuration and reload.
            Grasscutter.saveConfig(updated);
            Grasscutter.loadConfig();
        } catch (Exception exception) {
            Grasscutter.getLogger().warn("Failed to save the updated configuration.", exception);
        }
    }

    public Structure folderStructure = new Structure();
    public Database databaseInfo = new Database();
    public Language language = new Language();
    public Account account = new Account();
    public Server server = new Server();

    // DO NOT. TOUCH. THE VERSION NUMBER.
    public int version = version();

    /* Option containers. */

    public static class Database {
        public DataStore server = new DataStore();
        public DataStore game = new DataStore();

        public static class DataStore {
            public String connectionUri = "mongodb://localhost:27017";
            public String collection = "grasscutter";
        }
    }

    public static class Structure {
        public String resources = "./resources/";
        public String data = "./data/";
        public String packets = "./packets/";
        public String scripts = "resources:Scripts/";
        public String plugins = "./plugins/";
        public String cache = "./cache/";

        // UNUSED (potentially added later?)
        // public String dumps = "./dumps/";
    }

    public static class Server {
        public Set<Integer> debugWhitelist = Set.of();
        public Set<Integer> debugBlacklist = Set.of();
        public ServerRunMode runMode = ServerRunMode.HYBRID;
        public boolean logCommands = false;

        /**
         * If enabled, the 'require' Lua function will load the script's compiled varient into the context. (faster; doesn't work as well)
         * If disabled, all 'require' calls will be replaced with the referenced script's source. (slower; works better)
         */
        public boolean fastRequire = true;

        public HTTP http = new HTTP();
        public Game game = new Game();

        public Dispatch dispatch = new Dispatch();
        public DebugMode debugMode = new DebugMode();
    }

    public static class Language {
        public Locale language = Locale.getDefault();
        public Locale fallback = Locale.US;
        public String document = "EN";
    }

    public static class Account {
        public boolean autoCreate = false;
        public boolean EXPERIMENTAL_RealPassword = false;
        public String[] defaultPermissions = {};
        public int maxPlayer = -1;
    }

    /* Server options. */

    public static class HTTP {
        /* This starts the HTTP server before the game server. */
        public boolean startImmediately = false;

        public String bindAddress = "0.0.0.0";
        public int bindPort = 443;

        /* This is the address used in URLs. */
        public String accessAddress = "127.0.0.1";
        /* This is the port used in URLs. */
        public int accessPort = 0;

        public Encryption encryption = new Encryption();
        public Policies policies = new Policies();
        public Files files = new Files();
    }

    public static class Game {
        public String bindAddress = "0.0.0.0";
        public int bindPort = 22102;

        /* This is the address used in the default region. */
        public String accessAddress = "127.0.0.1";
        /* This is the port used in the default region. */
        public int accessPort = 0;

        /* Enabling this will generate a unique packet encryption key for each player. */
        public boolean useUniquePacketKey = true;

        /* Entities within a certain range will be loaded for the player */
        public int loadEntitiesForPlayerRange = 300;
        /* Start in 'unstable-quests', Lua scripts will be enabled by default. */
        public boolean enableScriptInBigWorld = true;
        public boolean enableConsole = true;

        /* Kcp internal work interval (milliseconds) */
        public int kcpInterval = 20;
        /* Controls whether packets should be logged in console or not */
        public ServerDebugMode logPackets = ServerDebugMode.NONE;
        /* Show packet payload in console or no (in any case the payload is shown in encrypted view) */
        public boolean isShowPacketPayload = false;
        /* Show annoying loop packets or no */
        public boolean isShowLoopPackets = false;

        public boolean cacheSceneEntitiesEveryRun = false;

        public GameOptions gameOptions = new GameOptions();
        public JoinOptions joinOptions = new JoinOptions();
        public ConsoleAccount serverAccount = new ConsoleAccount();

        public VisionOptions[] visionOptions = new VisionOptions[]{
            new VisionOptions("VISION_LEVEL_NORMAL", 80, 20),
            new VisionOptions("VISION_LEVEL_LITTLE_REMOTE", 16, 40),
            new VisionOptions("VISION_LEVEL_REMOTE", 1000, 250),
            new VisionOptions("VISION_LEVEL_SUPER", 4000, 1000),
            new VisionOptions("VISION_LEVEL_NEARBY", 40, 20),
            new VisionOptions("VISION_LEVEL_SUPER_NEARBY", 20, 20)
        };
    }

    /* Data containers. */

    public static class Dispatch {
        /* An array of servers. */
        public Region[] regions;

        public JsonObject resources;

        /* The URL used to make HTTP requests to the dispatch server. */
        public String dispatchUrl = "ws://127.0.0.1:1111";
        /* A unique key used for encryption. */
        public byte[] encryptionKey = Crypto.createSessionKey(32);
        /* A unique key used for authentication. */
        public String dispatchKey = Utils.base64Encode(
            Crypto.createSessionKey(32));

        public String defaultName = "Grasscutter";

        /* Controls whether http requests should be logged in console or not */
        public ServerDebugMode logRequests = ServerDebugMode.NONE;
    }

    /* Debug options container, used when jar launch argument is -debug | -debugall and override default values
     *  (see StartupArguments.enableDebug) */
    public static class DebugMode {
        /* Log level of the main server code (works only with -debug arg) */
        public Level serverLoggerLevel = Level.DEBUG;

        /* Log level of the third-party services (works only with -debug arg):
           javalin, quartz, reflections, jetty, mongodb.driver */
        public Level servicesLoggersLevel = Level.INFO;

        /* Controls whether packets should be logged in console or not */
        public ServerDebugMode logPackets = ServerDebugMode.ALL;

        /* Show packet payload in console or no (in any case the payload is shown in encrypted view) */
        public boolean isShowPacketPayload = false;

        /* Show annoying loop packets or no */
        public boolean isShowLoopPackets = false;

        /* Controls whether http requests should be logged in console or not */
        public ServerDebugMode logRequests = ServerDebugMode.ALL;
    }

    public static class Encryption {
        public boolean useEncryption = true;
        /* Should 'https' be appended to URLs? */
        public boolean useInRouting = true;
        public String keystore = "./keystore.p12";
        public String keystorePassword = "123456";
    }

    public static class Policies {
        public Policies.CORS cors = new Policies.CORS();

        public static class CORS {
            public boolean enabled = true;
            public String[] allowedOrigins = new String[]{"*"};
        }
    }

    public static class GameOptions {
        public InventoryLimits inventoryLimits = new InventoryLimits();
        public AvatarLimits avatarLimits = new AvatarLimits();
        public int sceneEntityLimit = 1000; // Unenforced. TODO: Implement.

        public boolean watchGachaConfig = false;
        public boolean enableShopItems = true;
        public boolean staminaUsage = true;
        public boolean energyUsage = true;
        public boolean fishhookTeleport = true;
        public boolean trialCostumes = false;

        @SerializedName(value = "questing", alternate = "questOptions")
        public Questing questing = new Questing();
        public ResinOptions resinOptions = new ResinOptions();
        public Rates rates = new Rates();

        public HandbookOptions handbook = new HandbookOptions();

        public static class InventoryLimits {
            public int weapons = 2000;
            public int relics = 2000;
            public int materials = 2000;
            public int furniture = 2000;
            public int all = 30000;
        }

        public static class AvatarLimits {
            public int singlePlayerTeam = 4;
            public int multiplayerTeam = 4;
        }

        public static class Rates {
            public float adventureExp = 1.0f;
            public float mora = 1.0f;
            public float leyLines = 1.0f;
        }

        public static class ResinOptions {
            public boolean resinUsage = false;
            public int cap = 160;
            public int rechargeTime = 480;
        }

        public static class Questing {
            /* Should questing behavior be used? */
            public boolean enabled = true;
        }

        public static class HandbookOptions {
            public boolean enable = false;
            public boolean allowCommands = true;

            public Limits limits = new Limits();
            public Server server = new Server();

            public static class Limits {
                /* Are rate limits checked? */
                public boolean enabled = false;
                /* The time for limits to expire. */
                public int interval = 3;

                /* The maximum amount of normal requests. */
                public int maxRequests = 10;
                /* The maximum amount of entities to be spawned in one request. */
                public int maxEntities = 25;
            }

            public static class Server {
                /* Are the server settings sent to the handbook? */
                public boolean enforced = false;
                /* The default server address for the handbook's authentication. */
                public String address = "127.0.0.1";
                /* The default server port for the handbook's authentication. */
                public int port = 443;
                /* Should the defaults be enforced? */
                public boolean canChange = true;
            }
        }
    }

    public static class VisionOptions {
        public String name;
        public int visionRange;
        public int gridWidth;

        public VisionOptions(String name, int visionRange, int gridWidth) {
            this.name = name;
            this.visionRange = visionRange;
            this.gridWidth = gridWidth;
        }
    }

    public static class JoinOptions {
        public int[] welcomeEmotes = {2007, 1002, 4010};
        public String welcomeMessage = "Welcome to a Grasscutter server.";
        public JoinOptions.Mail welcomeMail = new JoinOptions.Mail();

        public static class Mail {
            public String title = "Welcome to Grasscutter!";
            public String content = """
                Hi there!\r
                First of all, welcome to Grasscutter. If you have any issues, please let us know so that Lawnmower can help you! \r
                \r
                Check out our:\r
                <type="browser" text="Discord" href="https://discord.gg/T5vZU6UyeG"/>
                """;
            public String sender = "Lawnmower";
            public emu.grasscutter.game.mail.Mail.MailItem[] items = {
                new emu.grasscutter.game.mail.Mail.MailItem(13509, 1, 1),
                new emu.grasscutter.game.mail.Mail.MailItem(201, 99999, 1)
            };
        }
    }

    public static class ConsoleAccount {
        public int avatarId = 10000007;
        public int nameCardId = 210001;
        public int adventureRank = 1;
        public int worldLevel = 0;

        public String nickName = "Server";
        public String signature = "Welcome to Grasscutter!";
    }

    public static class Files {
        public String indexFile = "./index.html";
        public String errorFile = "./404.html";
    }

    /* Objects. */

    @NoArgsConstructor
    public static class Region {
        // Make preview config happy
        @SerializedName(value = "name", alternate = "Name")
        public String name = "os_usa";
        @SerializedName(value = "title", alternate = "Title")
        public String title = "Grasscutter";
        @SerializedName(value = "ip", alternate = "Ip")
        public String ip = "127.0.0.1";
        @SerializedName(value = "port", alternate = "Port")
        public int port = 22102;
        public boolean isEnableDownloadResource = true;
        public String[] versions; // regex for matching game versions

        public Region(
            String name, String title,
            String address, int port,
            String version
        ) {
            this.name = name;
            this.title = title;
            this.ip = address;
            this.port = port;
            this.versions = new String[]{version};
        }
    }

    public static class Resource {
        //pc
        public String resourceUrl = "https://autopatchhk.yuanshen.com/client_game_res/4.5_live";
        public String dataUrl = "https://autopatchhk.yuanshen.com/client_design_data/4.5_live";
        public String resourceUrlBak = "";
        public int clientDataVersion = 22465243;
        public int clientSilenceDataVersion = 21858521;
        public String clientDataMd5 = "{\\\"remoteName\\\": \\\"data_versions\\\", \\\"md5\\\": \\\"eb9b402016c76770ddc9d5a169dcfd48\\\", \\\"hash\\\": \\\"b16bd096d0c7d285\\\", \\\"fileSize\\\": 6523}\\r\\n{\\\"remoteName\\\": \\\"data_versions_medium\\\", \\\"md5\\\": \\\"b0ffcd21a88af82a2661e40cac115acf\\\", \\\"hash\\\": \\\"9b98e8158f002b39\\\", \\\"fileSize\\\": 6523}";
        public String clientSilenceDataMd5 = "{\\\"remoteName\\\": \\\"data_versions\\\", \\\"md5\\\": \\\"60cf7110693dda2976a447a18ac1d6a5\\\", \\\"hash\\\": \\\"e44e5dbd5ae7fb89\\\", \\\"fileSize\\\": 522}";
        public ResVersionConfig resVersionConfig = new ResVersionConfig();
        public String clientVersionSuffix = "b932e2497d";
        public String clientSilenceVersionSuffix = "d339155f05";
        public String nextResourceUrl = "";
        public ResVersionConfig nextResVersionConfig = new ResVersionConfig();
    }

    public static class ResVersionConfig {
        public int version = 22021066;
        public String md5 = "{\\\"remoteName\\\": \\\"res_versions_external\\\", \\\"md5\\\": \\\"adb3e12fbf717600f70ce4a0e0d46650\\\", \\\"hash\\\": \\\"ff18b361d876d892\\\", \\\"fileSize\\\": 2178038}\\r\\n{\\\"remoteName\\\": \\\"res_versions_medium\\\", \\\"md5\\\": \\\"ecf8d7a6ab1295ee34a80ab29edcbb84\\\", \\\"hash\\\": \\\"2e5aa69e9f956e91\\\", \\\"fileSize\\\": 313346}\\r\\n{\\\"remoteName\\\": \\\"res_versions_streaming\\\", \\\"md5\\\": \\\"aa1baf8e6e58d80b9676c686c4ea4d58\\\", \\\"hash\\\": \\\"ef54b41d05025f46\\\", \\\"fileSize\\\": 115479}\\r\\n{\\\"remoteName\\\": \\\"base_revision\\\", \\\"md5\\\": \\\"66474f0fca98d73dcb636deb9e96702d\\\", \\\"hash\\\": \\\"8650fd397330e2f1\\\", \\\"fileSize\\\": 19}";
        public String releaseTotalSize = "0";
        public String versionSuffix = "0ad2263aaa";
        public String branch = "4.5_live";
    }
}

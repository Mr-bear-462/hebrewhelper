package il.helper;

import java.util.*;

/** All dictionaries: items, mobs, enchantments, effects, build materials... */
final class Data {
    private Data() {}

    static final class Item {
        final String id; final int tier; final String he; boolean materialWord;
        Item(String id, int tier, String he) { this.id = id; this.tier = tier; this.he = he; }
    }
    static final class Mob {
        final String id; final int tier; final String he;
        Mob(String id, int tier, String he) { this.id = id; this.tier = tier; this.he = he; }
    }

    static final Map<String, String> PHRASES = new LinkedHashMap<>();
    static final Map<String, String> ORIG = new HashMap<>(); // phrase token -> original display text
    static final Map<String, Item> ITEMS = new HashMap<>();
    static final Map<String, Mob> MOBS = new HashMap<>();
    static final Map<String, String> ENCH = new HashMap<>();
    static final Map<String, Integer> ENCH_MAX = new HashMap<>();
    static final Map<String, String> ENCH_HE = new HashMap<>();
    static final Map<String, String> EFFECTS = new HashMap<>();
    static final Map<String, String> EFFECT_HE = new HashMap<>();
    static final Map<String, String> PIECES = new HashMap<>();   // word -> piece key
    static final Map<String, String> MATERIALS = new HashMap<>(); // word -> material key
    static final Map<String, String> STRUCTS = new HashMap<>();  // word -> structure key
    static final Map<String, String> BUILD_MATS = new HashMap<>(); // word -> block id
    static final Map<String, Integer> BUILD_MAT_TIER = new HashMap<>();
    static final Map<String, String> GAMEMODES = new HashMap<>();
    static final Map<String, String> RAW_BAD = new HashMap<>();   // dangerous raw ids -> tier marker

    /** Registers a multi-word phrase and returns the joined token. */
    static String P(String phrase) {
        String k = Hebrew.key(phrase);
        String tok = k.replace(' ', '_');
        if (!k.equals(tok)) PHRASES.put(k, tok);
        ORIG.put(tok, phrase);
        return tok;
    }

    private static boolean ascii(String s) {
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) > 127) return false;
        return true;
    }

    private static <T> void put(Map<String, T> m, String word, T v, boolean plural) {
        String k = Hebrew.key(word).replace(' ', '_');
        m.put(k, v);
        if (plural && !ascii(k) && k.indexOf('_') < 0 && k.length() >= 2) {
            m.putIfAbsent(Hebrew.key(k + "\u05D9\u05DD"), v);
            m.putIfAbsent(Hebrew.key(k + "\u05D5\u05EA"), v);
            if (k.endsWith("\u05D4")) m.putIfAbsent(k.substring(0, k.length() - 1) + "\u05D5\u05EA", v);
        }
    }

    private static String disp(String w) { return ORIG.getOrDefault(w, w).replace('_', ' '); }

    private static Item item(int tier, String id, String... words) {
        Item it = new Item(id, tier, disp(words[0]));
        for (String w : words) put(ITEMS, w, it, true);
        return it;
    }
    private static Mob mob(int tier, String id, String... words) {
        Mob m = new Mob(id, tier, disp(words[0]));
        for (String w : words) put(MOBS, w, m, true);
        return m;
    }
    private static void ench(String id, int max, String... words) {
        ENCH_MAX.put(id, max);
        ENCH_HE.put(id, disp(words[0]));
        for (String w : words) put(ENCH, w, id, false);
    }
    private static void eff(String id, String... words) {
        EFFECT_HE.put(id, disp(words[0]));
        for (String w : words) put(EFFECTS, w, id, false);
    }
    private static void piece(String key, String... words) { for (String w : words) put(PIECES, w, key, true); }
    private static void mat(String key, String... words) { for (String w : words) put(MATERIALS, w, key, false); }
    private static void struct(String key, String... words) { for (String w : words) put(STRUCTS, w, key, true); }
    private static void bm(String id, int tier, String... words) {
        BUILD_MAT_TIER.put(id, tier);
        for (String w : words) put(BUILD_MATS, w, id, false);
    }

    static {
        // ---------------- items: basic blocks & stuff (tier 0) ----------------
        item(0, "stone", "אבן", "stone");
        item(0, "cobblestone", P("אבן מרוצפת"), "קוביסטון", "cobblestone");
        item(0, "dirt", "אדמה", "עפר", "dirt");
        item(0, "grass_block", "דשא", "grass_block");
        item(0, "sand", "חול", "sand");
        item(0, "gravel", "חצץ", "gravel");
        item(0, "glass", "זכוכית", "glass");
        item(0, "bricks", "לבנים", "bricks");
        item(0, "oak_log", "גזע", P("בול עץ"), "עץ", "log");
        item(0, "oak_planks", "קרשים", "קרש", "planks");
        item(0, "stick", "מקל", "מקלות", "stick");
        item(0, "furnace", "כבשן", "תנור", "furnace");
        item(0, "crafting_table", P("שולחן עבודה"), "crafting_table");
        item(0, "chest", "תיבה", "ארגז", "chest");
        item(0, "oak_door", "דלת", "door");
        item(0, "ladder", "סולם", "ladder");
        item(0, "torch", "לפיד", "לפידים", "torch");
        item(0, "red_bed", "מיטה", "bed");
        item(0, "bucket", "דלי", "bucket");
        item(0, "water_bucket", P("דלי מים"), "water_bucket");
        item(1, "lava_bucket", P("דלי לבה"), "lava_bucket");
        item(0, "flint_and_steel", "מצית", "flint_and_steel");
        item(0, "shears", "מספריים", "shears");
        item(0, "clock", "שעון", "clock");
        item(0, "compass", "מצפן", "compass");
        item(0, "spyglass", "משקפת", "spyglass");
        item(0, "lead", "רצועה", "lead");
        item(0, "saddle", "אוכף", "saddle");
        item(0, "oak_boat", "סירה", "boat");
        item(0, "minecart", "עגלה", "minecart");
        item(0, "rail", P("פסי רכבת"), "פסים", "rail");
        item(0, "powered_rail", P("פסי הנעה"), "powered_rail");
        item(1, "tnt", "tnt", "טנטי", P("טי אן טי"), "דינמיט", "חומר נפץ");
        item(0, "wheat_seeds", "זרעים", "wheat_seeds");
        item(0, "wheat", "חיטה", "wheat");
        item(0, "oak_sapling", "שתיל", "sapling");
        item(0, "white_wool", "צמר", "wool");
        item(0, "ice", "קרח", "ice");
        item(0, "snowball", P("כדור שלג"), "snowball");
        item(0, "candle", "נר", "candle");
        item(0, "lantern", "פנס", "lantern");
        item(0, "glowstone", "גלואוסטון", "glowstone");
        item(0, "sea_lantern", P("פנס ים"), "sea_lantern");
        item(0, "piston", "פיסטון", "piston");
        item(0, "sticky_piston", P("פיסטון דביק"), "sticky_piston");
        item(0, "hopper", "הופר", "hopper");
        item(0, "dispenser", "דיספנסר", "dispenser");
        item(0, "dropper", "דרופר", "dropper");
        item(0, "redstone_lamp", "מנורה", P("מנורת רדסטון"), "redstone_lamp");
        item(0, "firework_rocket", "זיקוקים", "זיקוק", "firework_rocket");
        item(0, "book", "book");
        item(0, "bookshelf", P("כוננית ספרים"), "כוננית", "bookshelf");
        item(0, "anvil", "סדן", "anvil");
        item(0, "enchanting_table", P("שולחן קסמים"), "enchanting_table");
        item(0, "brewing_stand", P("מעמד חליטה"), "brewing_stand");
        item(0, "cauldron", "קלחת", "cauldron");
        item(0, "obsidian", "אובסידיאן", "obsidian");
        item(0, "netherrack", "נדרראק", "netherrack");
        item(0, "ender_chest", P("תיבת אנדר"), "ender_chest");
        item(1, "shulker_box", P("קופסת שולקר"), "שולקר", "shulker_box");
        item(0, "oak_sign", "שלט", "sign");
        item(0, "painting", "ציור", "painting");
        item(0, "item_frame", "מסגרת", "item_frame");
        item(0, "flower_pot", "עציץ", "flower_pot");
        item(0, "poppy", "פרח", "פרחים", "poppy");
        item(0, "pumpkin", "דלעת", "pumpkin");
        item(0, "cactus", "קקטוס", "cactus");
        item(0, "sugar_cane", P("קני סוכר"), "sugar_cane");
        item(0, "slime_ball", P("כדור סליים"), "slime_ball");

        // food
        item(0, "apple", "תפוח", "apple");
        item(0, "bread", "לחם", "bread");
        item(0, "carrot", "גזר", "carrot");
        item(0, "potato", P("תפוח אדמה"), "תפוחי_אדמה", "potato");
        item(0, "cooked_beef", "סטייק", "בשר", "steak");
        item(0, "cooked_porkchop", P("בשר חזיר"), "porkchop");
        item(0, "cooked_chicken", "עוף", "chicken_meat");
        item(0, "cooked_cod", "דג", "דגים", "fish");
        item(0, "cooked_salmon", "סלמון", "salmon");
        item(0, "cake", "עוגה", "cake");
        item(0, "cookie", "עוגיה", "עוגיות", "cookie");
        item(0, "melon_slice", "אבטיח", "melon");
        item(0, "honey_bottle", "דבש", "honey");
        item(0, "egg", "ביצה", "egg");
        item(0, "milk_bucket", "חלב", "milk");
        item(0, "pumpkin_pie", P("פאי דלעת"), "pumpkin_pie");
        item(1, "golden_apple", P("תפוח זהב"), P("תפוחי זהב"), "golden_apple");
        item(2, "enchanted_golden_apple", P("תפוח זהב מכושף"), P("תפוחי זהב מכושפים"), P("תפוח זהב קסום"), "notch_apple", "enchanted_golden_apple");
        item(1, "golden_carrot", P("גזר זהב"), "golden_carrot");

        // ores & materials
        Item iron = item(0, "iron_ingot", "ברזל", "iron");
        Item gold = item(0, "gold_ingot", "זהב", "gold");
        Item dia = item(1, "diamond", "יהלום", "diamond");
        Item stoneM = ITEMS.get(Hebrew.key("אבן"));
        item(1, "emerald", "אזמרגד", "אמרלד", "emerald");
        item(0, "coal", "פחם", "coal");
        item(0, "redstone", "רדסטון", "redstone");
        item(0, "lapis_lazuli", "לאפיס", "lapis");
        item(0, "quartz", "קוורץ", "quartz");
        item(0, "copper_ingot", "נחושת", "copper");
        item(2, "netherite_ingot", "נתרייט", "netherite_ingot", "netherite");
        item(2, "netherite_scrap", P("שבבי נתרייט"), "netherite_scrap");
        item(2, "netherite_upgrade_smithing_template", P("תבנית נתרייט"));
        item(1, "iron_block", P("בלוק ברזל"), "iron_block");
        item(1, "gold_block", P("בלוק זהב"), "gold_block");
        item(1, "diamond_block", P("בלוק יהלום"), "diamond_block");
        item(1, "emerald_block", P("בלוק אזמרגד"), P("בלוק אמרלד"), "emerald_block");
        item(2, "netherite_block", P("בלוק נתרייט"), "netherite_block");
        item(0, "coal_block", P("בלוק פחם"), "coal_block");
        item(0, "redstone_block", P("בלוק רדסטון"), "redstone_block");
        item(0, "lapis_block", P("בלוק לאפיס"), "lapis_block");
        iron.materialWord = true; gold.materialWord = true; dia.materialWord = true;
        if (stoneM != null) stoneM.materialWord = true;
        ITEMS.get(Hebrew.key("עץ")).materialWord = true;
        ITEMS.get("copper").materialWord = true;
        ITEMS.get("netherite").materialWord = true;

        // special / valuable
        item(1, "ender_pearl", P("פנינת אנדר"), "ender_pearl");
        item(1, "ender_eye", P("עין אנדר"), "ender_eye");
        item(1, "totem_of_undying", "טוטם", "totem");
        item(1, "elytra", "אליטרה", "כנפיים", "elytra");
        item(1, "trident", "טריידנט", "קלשון", "trident");
        item(2, "mace", "מייס", "אלת_הפגיעה", "mace");
        item(0, "bow", "קשת", "bow");
        item(0, "crossbow", P("קשת צולבת"), "crossbow");
        item(0, "arrow", "חץ", "חיצים", "arrow");
        item(0, "shield", "מגן", "shield");
        item(0, "fishing_rod", "חכה", P("חכת דיג"), "fishing_rod");
        item(2, "nether_star", P("כוכב נדר"), P("כוכב נתר"), "nether_star");
        item(2, "beacon", "משואה", "beacon");
        item(2, "dragon_egg", P("ביצת דרקון"), "dragon_egg");
        item(1, "diamond_horse_armor", P("שריון סוס"), "diamond_horse_armor");
        item(1, "experience_bottle", P("בקבוק ניסיון"), "experience_bottle");
        item(1, "enchanted_book", P("ספר קסום"), P("ספר מכושף"), "enchanted_book");
        item(1, "golden_horse_armor", P("שריון סוס זהב"));
        item(1, "sponge", "ספוג", "sponge");
        item(1, "conduit", "צינור_כוח", "conduit");
        item(1, "heart_of_the_sea", P("לב הים"), "heart_of_the_sea");
        item(1, "wind_charge", P("מטען רוח"), "wind_charge");
        item(2, "command_block", P("בלוק פקודה"), "command_block");
        item(2, "bedrock", "בדרוק", "bedrock");
        item(2, "barrier", "מחסום", "barrier");
        item(2, "debug_stick", P("מקל דיבאג"), "debug_stick");
        item(2, "structure_block", "structure_block");

        // ---------------- mobs ----------------
        mob(0, "pig", "חזיר", "pig");
        mob(0, "cow", "פרה", "cow");
        mob(0, "sheep", "כבשה", "sheep");
        mob(0, "chicken", "תרנגולת", "chicken");
        mob(0, "horse", "סוס", "horse");
        mob(0, "donkey", "חמור", "donkey");
        mob(0, "mule", "פרד", "mule");
        mob(0, "cat", "חתול", "חתולה", "cat");
        mob(0, "wolf", "זאב", "כלב", "wolf");
        mob(0, "rabbit", "ארנב", "rabbit");
        mob(0, "dolphin", "דולפין", "dolphin");
        mob(0, "parrot", "תוכי", "parrot");
        mob(0, "fox", "שועל", "fox");
        mob(0, "panda", "פנדה", "panda");
        mob(0, "turtle", "צב", "turtle");
        mob(0, "frog", "צפרדע", "frog");
        mob(0, "villager", "כפרי", "villager");
        mob(0, "mooshroom", "מושרום", "mooshroom");
        mob(0, "llama", "לאמה", "llama");
        mob(0, "camel", "גמל", "camel");
        mob(0, "axolotl", "אקסולוטל", "axolotl");
        mob(0, "bat", "עטלף", "bat");
        mob(0, "armadillo", "ארמדילו", "armadillo");
        mob(0, "iron_golem", P("גולם ברזל"), "iron_golem");
        mob(0, "snow_golem", P("גולם שלג"), "snow_golem");
        mob(0, "allay", "אלאי", "allay");
        mob(0, "bee", "דבורה", "bee");
        mob(0, "polar_bear", P("דוב קוטב"), "polar_bear");
        mob(1, "zombie", "זומבי", "zombie");
        mob(1, "skeleton", "שלד", "skeleton");
        mob(1, "creeper", "קריפר", "creeper");
        mob(1, "spider", "עכביש", "spider");
        mob(1, "enderman", "אנדרמן", "enderman");
        mob(1, "witch", "מכשפה", "witch");
        mob(1, "slime", "סליים", "slime");
        mob(1, "phantom", "פאנטום", "phantom");
        mob(1, "blaze", "בליז", "blaze");
        mob(1, "ghast", "גאסט", "ghast");
        mob(1, "piglin", "פיגלין", "piglin");
        mob(1, "pillager", "שודד", "pillager");
        mob(1, "vindicator", "וינדיקטור", "vindicator");
        mob(1, "drowned", "דרואנד", "drowned");
        mob(1, "husk", "הוסק", "husk");
        mob(1, "stray", "סטריי", "stray");
        mob(1, "magma_cube", P("קוביית מגמה"), "magma_cube");
        mob(1, "guardian", "גרדיאן", "guardian");
        mob(1, "vex", "וקס", "vex");
        mob(1, "lightning_bolt", "ברק", "ברקים", "lightning");
        mob(1, "tnt", "פיצוץ", "פיצוצים", "תפוצץ", "explosion");
        mob(2, "wither", "וויתר", "ויתר", "wither");
        mob(2, "ender_dragon", "דרקון", P("דרקון אנדר"), "ender_dragon", "dragon");
        mob(2, "warden", "וורדן", "warden");
        mob(2, "ravager", "ראוואג'ר", "ravager");
        mob(2, "elder_guardian", P("גרדיאן עתיק"), "elder_guardian");

        // dangerous raw ids (typed in English with underscore) need BESTIE
        for (String s : new String[]{"command_block","repeating_command_block","chain_command_block","structure_block",
                "structure_void","jigsaw","barrier","bedrock","end_portal_frame","spawner","trial_spawner","vault",
                "debug_stick","command_block_minecart","test_block","test_instance_block","light"}) RAW_BAD.put(s, "2");

        // ---------------- enchantments ----------------
        ench("protection", 4, "הגנה", "protection");
        ench("fire_protection", 4, "הגנה_מאש", P("הגנה מאש"), "fire_protection");
        ench("blast_protection", 4, P("הגנה מפיצוצים"), "blast_protection");
        ench("projectile_protection", 4, P("הגנה מקליעים"), "projectile_protection");
        ench("feather_falling", 4, P("נפילה רכה"), P("נפילה מרוסנת"), "feather_falling");
        ench("thorns", 3, "קוצים", "thorns");
        ench("respiration", 3, P("נשימה במים"), "respiration");
        ench("aqua_affinity", 1, P("זיקה למים"), "aqua_affinity");
        ench("depth_strider", 3, P("הליכה במים"), "depth_strider");
        ench("frost_walker", 2, P("הליכת קרח"), "frost_walker");
        ench("soul_speed", 3, P("מהירות נשמות"), "soul_speed");
        ench("swift_sneak", 3, P("התגנבות מהירה"), P("swift sneak"), "swift_sneak");
        ench("mending", 1, "מנדינג", P("תיקון עצמי"), "mending");
        ench("unbreaking", 3, "עמידות", "unbreaking");
        ench("sharpness", 5, "חדות", "sharpness");
        ench("smite", 5, "סמייט", "smite");
        ench("bane_of_arthropods", 5, P("מכת פרוקי רגליים"), P("bane of arthropods"), "bane_of_arthropods");
        ench("knockback", 2, "הדיפה", "knockback");
        ench("fire_aspect", 2, "הצתה", P("fire aspect"), "fire_aspect");
        ench("looting", 3, "ביזה", "looting");
        ench("sweeping_edge", 3, P("חיתוך שוטף"), P("sweeping edge"), "sweeping_edge");
        ench("efficiency", 5, "יעילות", "efficiency");
        ench("silk_touch", 1, P("מגע משי"), P("silk touch"), "silk_touch");
        ench("fortune", 3, "מזל", "fortune");
        ench("power", 5, "עוצמה", "כוח", "power");
        ench("punch", 2, "אגרוף", "punch");
        ench("flame", 1, "להבה", "flame");
        ench("infinity", 1, "אינסוף", "infinity");
        ench("luck_of_the_sea", 3, P("מזל הים"), P("luck of the sea"), "luck_of_the_sea");
        ench("lure", 3, "פיתוי", "lure");
        ench("loyalty", 3, "נאמנות", "loyalty");
        ench("riptide", 3, "ריפטייד", "riptide");
        ench("channeling", 1, "צ'אנלינג", "channeling");
        ench("impaling", 5, "דקירה", "impaling");
        ench("quick_charge", 3, P("טעינה מהירה"), P("quick charge"), "quick_charge");
        ench("multishot", 1, P("ירי מרובה"), "multishot");
        ench("piercing", 4, "חדירה", "piercing");
        ench("density", 5, "צפיפות", "density");
        ench("breach", 4, "פריצה", "breach");
        ench("wind_burst", 3, P("פרץ רוח"), P("wind burst"), "wind_burst");
        ench("lunge", 3, "lunge", "זינוק");
        ench("binding_curse", 1, P("קללת קשירה"), "binding_curse");
        ench("vanishing_curse", 1, P("קללת היעלמות"), "vanishing_curse");

        // ---------------- effects ----------------
        eff("speed", "מהירות", "speed");
        eff("jump_boost", "קפיצה", "קפיצות", "jump");
        eff("night_vision", P("ראיית לילה"), P("לראות בחושך"), "night_vision");
        eff("strength", "חוזק", "strength");
        eff("resistance", "התנגדות", "resistance");
        eff("water_breathing", P("נשימה מתחת למים"), "water_breathing");
        eff("fire_resistance", P("עמידות באש"), P("התנגדות לאש"), "fire_resistance");
        eff("regeneration", "התחדשות", "regeneration");
        eff("slow_falling", P("נפילה איטית"), "slow_falling");
        eff("invisibility", P("בלתי נראה"), "שקוף", "invisibility", "invisible");
        eff("glowing", "זוהר", "glowing");
        eff("health_boost", P("חיים נוספים"), "health_boost");
        eff("absorption", "ספיגה", "absorption");
        eff("haste", "זריזות", "haste");

        // ---------------- armor / tool pieces ----------------
        piece("helmet", "קסדה", "כובע", "helmet");
        piece("chestplate", "חולצה", P("שריון חזה"), "חזייה", "chestplate");
        piece("leggings", "מכנסיים", "מכנס", "leggings");
        piece("boots", "מגפיים", "מגף", "נעליים", "boots");
        piece("sword", "חרב", "sword");
        piece("axe", "גרזן", "axe");
        piece("pickaxe", "מכוש", "מעדר_כרייה", "pickaxe");
        piece("shovel", "אתי", "מחפרת", "shovel");
        piece("hoe", "מעדר", "hoe");
        piece("spear", "חנית", "spear");
        piece("armor", "ארמור", "armor", "armour", "שריון", "שריונות");
        piece("tools", "כלים", "tools");

        mat("netherite", "נתרייט", "נתרית", "netherite");
        mat("diamond", "יהלום", "diamond");
        mat("iron", "ברזל", "iron");
        mat("golden", "זהב", "זהוב", "gold", "golden");
        mat("stone", "אבן", "stone");
        mat("wooden", "עץ", "wood", "wooden");
        mat("leather", "עור", "leather");
        mat("chainmail", "שרשרת", "שרשראות", "chain", "chainmail");
        mat("copper", "נחושת", "copper");

        // ---------------- structures ----------------
        struct("house", "בית", "house", "קוטג'", "קוטג");
        struct("tower", "מגדל", "tower");
        struct("platform", "פלטפורמה", "במה", "platform");
        struct("wall", "קיר", "חומה", "wall");
        struct("pyramid", "פירמידה", "pyramid");
        struct("pool", "בריכה", "pool");
        struct("castle", "טירה", "מבצר", "ארמון", "castle");

        bm("oak_planks", 0, "עץ", "קרשים", "wood", "wooden");
        bm("stone_bricks", 0, "אבן", "אבנים", "stone");
        bm("cobblestone", 0, "קוביסטון", "cobblestone");
        bm("bricks", 0, "לבנים", "bricks");
        bm("glass", 0, "זכוכית", "glass");
        bm("sandstone", 0, "חול", "חולית", "sandstone");
        bm("dirt", 0, "אדמה", "dirt");
        bm("white_wool", 0, "צמר", "wool");
        bm("quartz_block", 0, "קוורץ", "quartz");
        bm("white_concrete", 0, "בטון", "concrete");
        bm("ice", 0, "קרח", "ice");
        bm("iron_block", 1, "ברזל", "iron");
        bm("copper_block", 1, "נחושת", "copper");
        bm("obsidian", 1, "אובסידיאן", "obsidian");
        bm("gold_block", 1, "זהב", "gold");
        bm("emerald_block", 1, "אזמרגד", "אמרלד", "emerald");
        bm("diamond_block", 2, "יהלום", "יהלומים", "diamond");
        bm("netherite_block", 2, "נתרייט", "netherite");

        gm("survival", "הישרדות", "survival");
        gm("creative", "יצירתי", "קריאייטיב", "creative");
        gm("adventure", "הרפתקה", "adventure");
        gm("spectator", "צופה", "ספקטייטור", "spectator");
    }

    private static void gm(String id, String... words) { for (String w : words) put(GAMEMODES, w, id, false); }

    // ---------------- which enchants fit which item category ----------------
    static final Map<String, List<String>> ENCH_OK = new HashMap<>();
    static final Map<String, String[]> BEST = new HashMap<>(); // category -> "id:level" best set

    private static List<String> L(String... a) { return Arrays.asList(a); }
    private static void ok(String cat, String base, String... extra) {
        List<String> l = new ArrayList<>(Arrays.asList(base.split(",")));
        l.addAll(Arrays.asList(extra));
        ENCH_OK.put(cat, l);
    }
    static {
        String armor = "protection,fire_protection,blast_protection,projectile_protection,thorns,unbreaking,mending,binding_curse,vanishing_curse";
        ok("helmet", armor, "respiration", "aqua_affinity");
        ok("chestplate", armor);
        ok("leggings", armor, "swift_sneak");
        ok("boots", armor, "feather_falling", "depth_strider", "frost_walker", "soul_speed");
        ok("sword", "sharpness,smite,bane_of_arthropods,knockback,fire_aspect,looting,sweeping_edge,unbreaking,mending,vanishing_curse");
        ok("axe", "sharpness,smite,bane_of_arthropods,efficiency,silk_touch,fortune,unbreaking,mending,vanishing_curse");
        ok("pickaxe", "efficiency,silk_touch,fortune,unbreaking,mending,vanishing_curse");
        ok("shovel", "efficiency,silk_touch,fortune,unbreaking,mending,vanishing_curse");
        ok("hoe", "efficiency,silk_touch,fortune,unbreaking,mending,vanishing_curse");
        ok("spear", "sharpness,smite,bane_of_arthropods,knockback,fire_aspect,looting,lunge,unbreaking,mending,vanishing_curse");
        ok("bow", "power,punch,flame,infinity,unbreaking,mending,vanishing_curse");
        ok("crossbow", "quick_charge,multishot,piercing,unbreaking,mending,vanishing_curse");
        ok("trident", "loyalty,riptide,channeling,impaling,unbreaking,mending,vanishing_curse");
        ok("fishing_rod", "luck_of_the_sea,lure,unbreaking,mending,vanishing_curse");
        ok("mace", "density,breach,wind_burst,smite,bane_of_arthropods,fire_aspect,unbreaking,mending,vanishing_curse");
        ok("durable", "unbreaking,mending,vanishing_curse");

        BEST.put("helmet", new String[]{"protection:4","respiration:3","aqua_affinity:1","thorns:3","unbreaking:3","mending:1"});
        BEST.put("chestplate", new String[]{"protection:4","thorns:3","unbreaking:3","mending:1"});
        BEST.put("leggings", new String[]{"protection:4","swift_sneak:3","thorns:3","unbreaking:3","mending:1"});
        BEST.put("boots", new String[]{"protection:4","feather_falling:4","depth_strider:3","soul_speed:3","unbreaking:3","mending:1"});
        BEST.put("sword", new String[]{"sharpness:5","fire_aspect:2","looting:3","sweeping_edge:3","unbreaking:3","mending:1"});
        BEST.put("axe", new String[]{"sharpness:5","efficiency:5","unbreaking:3","mending:1"});
        BEST.put("pickaxe", new String[]{"efficiency:5","fortune:3","unbreaking:3","mending:1"});
        BEST.put("shovel", new String[]{"efficiency:5","fortune:3","unbreaking:3","mending:1"});
        BEST.put("hoe", new String[]{"efficiency:5","fortune:3","unbreaking:3","mending:1"});
        BEST.put("spear", new String[]{"sharpness:5","fire_aspect:2","lunge:3","unbreaking:3","mending:1"});
        BEST.put("bow", new String[]{"power:5","punch:2","flame:1","infinity:1","unbreaking:3"});
        BEST.put("crossbow", new String[]{"quick_charge:3","piercing:4","unbreaking:3","mending:1"});
        BEST.put("trident", new String[]{"loyalty:3","impaling:5","channeling:1","unbreaking:3","mending:1"});
        BEST.put("fishing_rod", new String[]{"lure:3","luck_of_the_sea:3","unbreaking:3","mending:1"});
        BEST.put("mace", new String[]{"density:5","breach:4","fire_aspect:2","unbreaking:3","mending:1"});
        BEST.put("durable", new String[]{"unbreaking:3","mending:1"});
    }

    /** Category of an item id for enchanting, or null if it can't be enchanted. */
    static String category(String id) {
        if (id.endsWith("_helmet")) return "helmet";
        if (id.endsWith("_chestplate")) return "chestplate";
        if (id.endsWith("_leggings")) return "leggings";
        if (id.endsWith("_boots")) return "boots";
        if (id.endsWith("_sword")) return "sword";
        if (id.endsWith("_pickaxe")) return "pickaxe";
        if (id.endsWith("_axe")) return "axe";
        if (id.endsWith("_shovel")) return "shovel";
        if (id.endsWith("_hoe")) return "hoe";
        if (id.endsWith("_spear")) return "spear";
        switch (id) {
            case "bow": return "bow";
            case "crossbow": return "crossbow";
            case "trident": return "trident";
            case "fishing_rod": return "fishing_rod";
            case "mace": return "mace";
            case "elytra": case "shield": case "shears": case "flint_and_steel": case "turtle_helmet": return "durable";
            default: return null;
        }
    }

    static final String[] HOSTILES = {"zombie","skeleton","creeper","spider","enderman","witch","pillager","husk","stray",
            "drowned","slime","phantom","vindicator","magma_cube","blaze","guardian","zombified_piglin"};
}

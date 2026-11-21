package il.helper;

import java.util.*;
import java.util.regex.*;

/**
 * The "AI": understands Hebrew (and some English) sentences by combining word lists,
 * keeps a mood per player, and produces chat lines + vanilla commands. No Minecraft classes here.
 */
final class Brain {
    static final String NAME = "עוזר";

    static final class Cmd {
        final String run, fallback, failMsg;
        Cmd(String run, String fallback, String failMsg) { this.run = run; this.fallback = fallback; this.failMsg = failMsg; }
    }
    static final class Reply {
        final List<String> say = new ArrayList<>();
        final List<Cmd> cmds = new ArrayList<>();
    }
    static final class Info {
        double health = 20; int food = 20; int x, y, z; List<String> players = new ArrayList<>();
    }

    // -------- word sets --------
    private static final Set<String> ALIASES = Hebrew.set("עוזר", "עוזרת", "helper", "בוט", "bot");
    private static final Set<String> POLITE = Hebrew.set("בבקשה", "אנא", "אשמח", Data.P("אם אפשר"), "please", "pls", "plz", "בבקשהה");
    private static final Set<String> THANKS = Hebrew.set("תודה", "תודות", "תודה_רבה", "thanks", "thx", "thank", "מעריך");
    private static final Set<String> PRAISE = Hebrew.set("אלוף", "מלך", "גאון", "נהדר", "מדהים", "מושלם", "תותח", "אחלה",
            Data.P("כל הכבוד"), "מעולה", "מצוין", "אדיר", "מקסים", "חמוד", "אהוב", Data.P("אתה הכי טוב"), "אתה_הכי_טוב", "best", "great", "awesome");
    private static final Set<String> SORRY = Hebrew.set("סליחה", "מצטער", "מצטערת", "סורי", "sorry", "מתנצל", "סלח");
    private static final Set<String> RUDE = Hebrew.set("מטומטם", "אידיוט", "טיפש", "טפש", "דביל", "מפגר", "זבל", "סתום", "שתוק",
            "מעצבן", "גרוע", "חרא", "מכוער", "מגעיל", "נודניק", "פחדן", "עצלן", "stupid", "idiot", "dumb", "suck", "sucks", "trash", "useless", "shut", "חסר_תועלת", Data.P("חסר תועלת"));

    private static final Set<String> ASK_HELP = Hebrew.set("עזרה", "help", "פקודות", Data.P("מה אתה יודע"), Data.P("מה אתה יכול"), Data.P("איך משתמשים"));
    private static final Set<String> ASK_WHO = Hebrew.set(Data.P("מי אתה"), Data.P("איך קוראים לך"), Data.P("מה שמך"), "whoami");
    private static final Set<String> JOKE = Hebrew.set("בדיחה", "joke", "בדיחות");
    private static final Set<String> GREET = Hebrew.set("שלום", "היי", "הי", "אהלן", "hello", "hi", "hey", Data.P("מה נשמע"), Data.P("מה קורה"), Data.P("בוקר טוב"), Data.P("ערב טוב"), "אהלה", "שלומות");
    private static final Set<String> HOWRU = Hebrew.set(Data.P("מה שלומך"), Data.P("מה העניינים"), Data.P("איך אתה"), Data.P("מה איתך"), "howru");
    private static final Set<String> BYE = Hebrew.set("ביי", "להתראות", Data.P("לילה טוב"), "bye", "נתראה");
    private static final Set<String> STATUS_PH = Hebrew.set(Data.P("איפה אני"), Data.P("כמה חיים"), Data.P("מה שלומי"), "מיקום", "קואורדינטות", "coords", "status");

    private static final Set<String> BUILD_V = Hebrew.set("בנה", "תבנה", "לבנות", "תבנו", "בני", "תבני", "בנייה", "בניה", "build", "הקם", "תקים", "תקים_לי", "תעשה", "עשה");
    private static final Set<String> TP_V = Hebrew.set("טלפורט", "tp", "teleport", Data.P("קח אותי"), "קח", "תקח", "תיקח", "תעביר", "העבר", "תעלה", "העלה", "למעלה");
    private static final Set<String> KILL_V = Hebrew.set("תהרוג", "הרוג", "תנקה", "נקה", "השמד", "תחסל", "חסל", "kill", "clear");
    private static final Set<String> MONSTERS = Hebrew.set("מפלצות", "מפלצת", "אויבים", "מוביס", "mobs", "monsters", "enemies", "אנשי_רשע");
    private static final Set<String> HEAL = Hebrew.set("ריפוי", "תרפא", "רפא", "heal", Data.P("תחזיר חיים"), Data.P("תחזיר לי חיים"));
    private static final Set<String> FEED = Hebrew.set("האכל", "תאכיל", "תשביע", "רעב", "רעבים", "שובע", "feed", "hungry");
    private static final Set<String> XP = Hebrew.set("ניסיון", "xp", "רמות", "רמה", "levels", "אקספי");
    private static final Set<String> DIFF_W = Hebrew.set("קושי", "difficulty");
    private static final Set<String> DAY = Hebrew.exactSet("יום", "ליום", "day", "daytime", "בוקר", "לבוקר");
    private static final Set<String> NIGHT = Hebrew.exactSet("לילה", "ללילה", "night", "תחשיך", "חושך", "לחושך");
    private static final Set<String> NOON = Hebrew.exactSet("צהריים", "לצהריים", "noon", "צהרים", "לצהרים");
    private static final Set<String> MIDNIGHT = Hebrew.exactSet("חצות", "לחצות", "midnight");
    private static final Set<String> RAIN = Hebrew.exactSet("גשם", "גשמים", "לגשם", "הגשם", "rain");
    private static final Set<String> STORM = Hebrew.exactSet("סערה", "לסערה", "הסערה", "רעמים", "storm", "thunder");
    private static final Set<String> CLEAR = Hebrew.exactSet("שמש", "שמשי", "השמש", "בהיר", "בהירים", "clear", "sunny");
    private static final Set<String> STOP_W = Hebrew.set("תפסיק", "הפסק", "תעצור", "עצור", "stop", "בלי", "די");
    private static final Set<String> CHANGE_V = Hebrew.set("שנה", "תשנה", "תעשה", "עשה", "תהפוך", "הפוך", "תחשיך", "תאיר", "תעביר", "העבר", "make", "set", "change");
    private static final Set<String> SUMMON_V = Hebrew.set("תזמן", "זמן", "זימון", "summon", "spawn", "תיצור", "צור", "תוציא");
    private static final Set<String> BEST_W = Hebrew.set("מכושף", "מכושפת", "מכושפים", "מקוסם", "מקוסמת", "קסום", "enchanted", "maxed",
            "מקסימום", "מקס", "max", "god", Data.P("כל הקסמים"), "הקסמים", "קסמים");
    private static final Set<String> LEVEL_W = Hebrew.set("רמה", "רמת", "level", "lvl", "lv");

    private static final Pattern COORDS = Pattern.compile("(-?\\d+)[\\s,]+(-?\\d+)[\\s,]+(-?\\d+)");

    // -------- state --------
    private final Map<String, Integer> mood = new HashMap<>();
    private final Map<String, Long> active = new HashMap<>();
    private final Random rnd = new Random();

    int moodScore(String uuid) { return mood.getOrDefault(uuid, 0); }
    static int tierOf(int s) { return s <= -3 ? 0 : s <= 1 ? 1 : s <= 5 ? 2 : 3; }

    /** Was the helper called by name, or is this player in an ongoing conversation (3 minutes)? */
    boolean addressed(String uuid, String text, long now) {
        List<String> t = Hebrew.tokens(text, Data.PHRASES);
        for (String w : t) {
            if (Hebrew.in(ALIASES, w)) { active.put(uuid, now + 180_000L); return true; }
        }
        Long until = active.get(uuid);
        if (until != null && until > now && !t.isEmpty()) { active.put(uuid, now + 180_000L); return true; }
        return false;
    }

    private String pick(String... a) { return a[rnd.nextInt(a.length)]; }

    // =====================================================================================
    Reply handle(String uuid, String text, Info info) {
        Reply r = new Reply();
        List<String> toks = Hebrew.tokens(text, Data.PHRASES);
        toks.removeIf(w -> Hebrew.in(ALIASES, w));
        String raw = text.toLowerCase(Locale.ROOT);

        // ---- mood ----
        int old = moodScore(uuid), oldTier = tierOf(old);
        int d = 0, rude = 0; boolean sorry = false;
        for (String w : toks) {
            if (Hebrew.in(POLITE, w)) d += 1;
            if (Hebrew.in(THANKS, w)) d += 2;
            if (Hebrew.in(PRAISE, w)) d += 1;
            if (Hebrew.in(SORRY, w)) sorry = true;
            if (Hebrew.in(RUDE, w)) rude++;
        }
        d = Math.min(d, 3);
        if (oldTier == 0 && !sorry) d = Math.min(d, 1);
        if (rude > 0) d = -Math.min(rude * 3, 6);
        if (sorry && rude == 0) d += 3;
        int score = Math.max(-10, Math.min(10, old + d));
        mood.put(uuid, score);
        int tier = tierOf(score);

        if (tier > oldTier && tier == 3) r.say.add(pick("עכשיו אתה חבר אמיתי! מגיע לך את הטוב ביותר.", "וואו, איזה נימוס! אני מרגיש שאנחנו חברים טובים. אני נותן הכל."));
        else if (tier > oldTier && tier == 2) r.say.add(pick("אה, ככה זה יותר נעים! אני מתחיל לחבב אותך.", "תודה על הנימוס. אני כבר מוכן לתת דברים טובים."));
        else if (tier > oldTier && tier == 1) r.say.add(pick("בסדר, סולחים. אז מה אתה צריך?", "טוב, נרגעתי. מה רצית?"));
        else if (tier < oldTier && tier == 0) r.say.add(pick("וואו, איזה חוצפה! עכשיו אני לא משתף פעולה.", "אתה יודע מה? תבקש יפה ואז נדבר."));
        else if (tier < oldTier) r.say.add(pick("לא יפה... אני נעלב קצת.", "אוי, זה לא היה נחמד."));

        boolean hasBuild = false;

        // ---- what does the message ask for? ----
        boolean tpVerb = anyIn(toks, TP_V);
        Matcher cm = COORDS.matcher(raw);
        boolean coords = cm.find();

        // scan things (items / mobs / pieces / enchants)
        Scan sc = scan(toks);
        String struct = null; String structMat = null;
        for (String w : toks) { String s = Hebrew.find(Data.STRUCTS, w); if (s != null) { struct = s; break; } }
        boolean buildVerb = anyIn(toks, BUILD_V);
        boolean wantsBuild = struct != null && (buildVerb || (!anyIn(toks, Hebrew.set("תן", "תביא", "הבא")) && !tpVerb && !anyIn(toks, Hebrew.set("הבית", "לבית"))));
        if (struct != null && !buildVerb) wantsBuild = false; // "בית" alone is not a request
        boolean giveVerb = anyIn(toks, Hebrew.set("תן", "תביא", "הבא", "תשיג", "give", "תני"));
        if (wantsBuild && !giveVerb) {
            // words like "מזכוכית" / "מאבן" describe the building material, they are not items to give
            sc.things.removeIf(t -> t.item != null && t.piece == null && t.mob == null && t.rawId == null && Hebrew.find(Data.BUILD_MATS, t.word) != null);
        }

        List<String> actions = new ArrayList<>();
        boolean timeAsk = false;
        String timeCmd = null, timeHe = null;
        boolean noGreetLock = !(anyIn(toks, Hebrew.set("טוב")) && !anyIn(toks, CHANGE_V));
        if (noGreetLock) {
            if (exact(toks, NOON)) { timeCmd = "time set noon"; timeHe = "צהריים"; }
            else if (exact(toks, MIDNIGHT)) { timeCmd = "time set midnight"; timeHe = "חצות"; }
            else if (exact(toks, NIGHT)) { timeCmd = "time set night"; timeHe = "לילה"; }
            else if (exact(toks, DAY)) { timeCmd = "time set day"; timeHe = "יום"; }
        }
        String weatherCmd = null, weatherHe = null;
        if (exact(toks, STORM)) { weatherCmd = "weather thunder"; weatherHe = "סערה"; }
        else if (exact(toks, RAIN)) {
            if (anyIn(toks, STOP_W)) { weatherCmd = "weather clear"; weatherHe = "שמיים בהירים"; }
            else { weatherCmd = "weather rain"; weatherHe = "גשם"; }
        } else if (exact(toks, CLEAR)) { weatherCmd = "weather clear"; weatherHe = "שמיים בהירים"; }

        String gm = null;
        for (String w : toks) { String g = Hebrew.find(Data.GAMEMODES, w); if (g != null) { gm = g; break; } }
        boolean gmCtx = anyIn(toks, Hebrew.set("מצב", "gamemode", "gm", "שנה", "תשנה", "תעביר", "תהפוך", "תעשה", "עבור"));
        if (gm != null && !gmCtx) gm = null;

        String diff = null;
        if (anyIn(toks, DIFF_W)) {
            if (anyIn(toks, Hebrew.set("שלו", "שלווה", "peaceful", "שליו"))) diff = "peaceful";
            else if (anyIn(toks, Hebrew.set("קל", "easy"))) diff = "easy";
            else if (anyIn(toks, Hebrew.set("בינוני", "רגיל", "normal"))) diff = "normal";
            else if (anyIn(toks, Hebrew.set("קשה", "hard", "קשים"))) diff = "hard";
        }

        boolean statusAsk = anyIn(toks, STATUS_PH) || (anyIn(toks, Hebrew.set("מצב")) && anyIn(toks, Hebrew.set("מה")) && gm == null);
        boolean wantHelp = anyIn(toks, ASK_HELP);
        boolean wantWho = anyIn(toks, ASK_WHO);
        boolean wantJoke = anyIn(toks, JOKE);
        boolean wantKill = anyIn(toks, KILL_V) && anyIn(toks, MONSTERS);
        boolean wantHeal = anyIn(toks, HEAL);
        boolean wantFeed = anyIn(toks, FEED);
        boolean wantXp = anyIn(toks, XP) && !wantKill;
        List<String> effects = new ArrayList<>();
        for (String w : toks) { String e = Hebrew.find(Data.EFFECTS, w); if (e != null && !effects.contains(e)) effects.add(e); }
        boolean upReq = tpVerb && anyIn(toks, Hebrew.set("תעלה", "העלה", "למעלה", "up"));
        String tpName = null;
        for (String p : info.players) {
            Set<String> one = Set.of(Hebrew.key(p));
            for (String w : toks) if (Hebrew.in(one, w)) { tpName = p; break; }
            if (tpName != null) break;
        }
        boolean wantTp = tpVerb && (coords || tpName != null || upReq);

        boolean anyRequest = !sc.things.isEmpty() || wantsBuild || timeCmd != null || weatherCmd != null || gm != null || diff != null
                || wantKill || wantHeal || wantFeed || wantXp || !effects.isEmpty() || wantTp;

        // ---------------- angry: refuse / troll ----------------
        if (tier == 0) {
            if (anyRequest) {
                if (rnd.nextInt(100) < 35) {
                    r.say.add(pick("קח, זה מה שמגיע לך.", "הנה מתנה מיוחדת בשבילך, בזכות הנימוס שלך.", "תיהנה. זה בדיוק בגובה הנימוס שלך."));
                    r.cmds.add(new Cmd("give " + uuid + " dirt 1", null, null));
                } else {
                    r.say.add(pick("לא בא לי. תבקש יפה.", "חצוף! אולי תגיד 'בבקשה' קודם?", "אני לא עובד אצלך. נסה שוב בנימוס.", "הממ... לא. תתנצל ואז נדבר.", "מה אני, משרת שלך? תגיד בבקשה."));
                }
            } else if (rude == 0) {
                r.say.add(pick("מה אתה רוצה? אני עדיין נעלב.", "אני לא במצב רוח. תתנצל.", "תגיד 'סליחה' ואולי אשתפר."));
            } else {
                r.say.add(pick("ועוד פעם? אתה מעצבן.", "וואו, איזה חוצפה!", "אתה פשוט לא מבין, נכון?"));
            }
            return r;
        }

        // ---------------- information ----------------
        if (wantHelp) {
            r.say.add("אני מבין עברית! אפשר לבקש ממני כך (תתחיל בשם שלי: עוזר):");
            r.say.add("- דברים: 'תביא לי 5 יהלומים', 'תביא ארמור עם mending ו-swift sneak 100'");
            r.say.add("- יצורים: 'תזמן 3 חזירים', 'תזמן קריפר'");
            r.say.add("- בנייה: 'תבנה בית', 'תבנה מגדל מאבן', 'תבנה פירמידה'");
            r.say.add("- עולם: 'שנה ללילה', 'תעשה גשם', 'מצב יצירתי', 'קח אותי ל 100 70 -200'");
            r.say.add("- אני: 'מה המצב?', 'תרפא אותי', 'מהירות', 'תן 10 רמות'");
            r.say.add("והכי חשוב: תהיה נחמד אלי. כשאתה מתנהג יפה אני נותן דברים הרבה יותר טובים.");
            return r;
        }
        if (wantWho) {
            r.say.add(pick("אני העוזר שלך! אני מבין עברית, בונה, מזמן, נותן דברים ועוד. כתוב 'עוזר עזרה'.", "קוראים לי עוזר. תגיד מה אתה צריך, ובבקשה תהיה נחמד."));
            return r;
        }
        if (wantJoke) {
            r.say.add(pick("למה קריפר הוא חבר גרוע? כי הוא תמיד מתפוצץ באמצע השיחה.",
                    "מה אמר השלד לזומבי? 'אין לי עצם לסכסוך איתך.'",
                    "למה אנדרמן לא מצליח לשחק מחבואים? כי הוא תמיד מבקש לדבר איתו בעיניים.",
                    "למה הכפרי מתלונן? כי כולם אומרים לו 'הרמם' ואף אחד לא מבין אותו.",
                    "מה עושה דג שמרגיש לבד בבריכה? שולח מים בהודעה."));
            return r;
        }
        if (statusAsk && !wantsBuild) {
            String m = tier == 3 ? "חבר אמיתי" : tier == 2 ? "ידידותי" : "רגיל";
            r.say.add(String.format("המצב שלך: חיים %.0f מתוך 20, רעב %d מתוך 20.", info.health, info.food));
            r.say.add(String.format("אתה נמצא ב-X=%d Y=%d Z=%d. אני מרגיש כלפיך: %s.", info.x, info.y, info.z, m));
        }

        List<String> okLines = new ArrayList<>();
        List<String> noteLines = new ArrayList<>();
        int allowedMat = Math.max(0, tier - 1);

        // ---------------- build ----------------
        if (wantsBuild) {
            int need = struct.equals("castle") || struct.equals("pyramid") ? 2 : 1;
            String mat = null; int matTier = 0;
            for (String w : toks) { String b = Hebrew.find(Data.BUILD_MATS, w); if (b != null) { mat = b; break; } }
            if (mat != null) matTier = Data.BUILD_MAT_TIER.getOrDefault(mat, 0);
            String he = hebStruct(struct);
            if (tier < need) {
                noteLines.add(pick("את ה" + he + " הגדולה אני בונה רק לחברים טובים. תגיד 'בבקשה' ו'תודה' ונראה.", "זה פרויקט גדול... תתנהג יפה ואבנה לך."));
            } else {
                if (mat != null && matTier > allowedMat) {
                    noteLines.add("חומר כזה יקר שמור לחברים טובים. אבנה מחומר רגיל.");
                    mat = null;
                }
                if (mat == null) mat = defaultMat(struct);
                buildCmds(r, uuid, struct, mat, tier);
                okLines.add("בניתי " + he + " מולך. תסתכל קדימה!");
                hasBuild = true;
            }
        }

        // ---------------- give / summon ----------------
        if (!sc.things.isEmpty()) {
            giveAndSummon(r, uuid, sc, tier, toks, okLines, noteLines);
        }

        // ---------------- world & player ----------------
        if (timeCmd != null) { r.cmds.add(new Cmd(timeCmd, null, null)); okLines.add("שיניתי ל" + timeHe + "."); }
        if (weatherCmd != null) { r.cmds.add(new Cmd(weatherCmd, null, null)); okLines.add("עשיתי " + weatherHe + "."); }
        if (gm != null) {
            if ((gm.equals("creative") || gm.equals("spectator")) && tier < 2) {
                noteLines.add("מצב " + (gm.equals("creative") ? "יצירתי" : "צופה") + " אני נותן רק לחברים. תגיד בבקשה ותודה.");
            } else {
                r.cmds.add(new Cmd("gamemode " + gm + " " + uuid, null, null));
                okLines.add("שיניתי את מצב המשחק שלך.");
            }
        }
        if (diff != null) { r.cmds.add(new Cmd("difficulty " + diff, null, null)); okLines.add("שיניתי את הקושי."); }
        if (wantHeal) { r.cmds.add(new Cmd("effect give " + uuid + " instant_health 1 10 true", null, null));
            r.cmds.add(new Cmd("effect give " + uuid + " saturation 3 10 true", null, null)); okLines.add("ריפאתי אותך והאכלתי אותך."); }
        else if (wantFeed) { r.cmds.add(new Cmd("effect give " + uuid + " saturation 3 10 true", null, null)); okLines.add("האכלתי אותך."); }
        if (wantXp) {
            int lv = 10; for (int i = 0; i < toks.size(); i++) { int[] n = Hebrew.number(toks, i); if (n != null && n[0] > 0) { lv = n[0]; break; } }
            int cap = tier == 1 ? 10 : tier == 2 ? 50 : 99;
            if (lv > cap) { lv = cap; noteLines.add("עד " + cap + " רמות בכל פעם."); }
            r.cmds.add(new Cmd("xp add " + uuid + " " + lv + " levels", null, null)); okLines.add("נתתי לך " + lv + " רמות.");
        }
        if (!effects.isEmpty()) {
            int secs = tier == 1 ? 120 : tier == 2 ? 600 : 1800;
            int ampCap = tier == 1 ? 1 : tier == 2 ? 4 : 9;
            int lvl = -1; for (int i = 0; i < toks.size(); i++) { int[] n = Hebrew.number(toks, i); if (n != null && n[0] > 0) { lvl = n[0]; break; } }
            int amp = lvl < 0 ? (tier == 1 ? 0 : 1) : Math.min(lvl - 1, ampCap);
            for (String e : effects) {
                if (e.equals("invisibility") && tier < 2) { noteLines.add("בלתי נראה רק לחברים."); continue; }
                r.cmds.add(new Cmd("effect give " + uuid + " " + e + " " + secs + " " + amp + " true", null, null));
                okLines.add("נתתי לך " + Data.EFFECT_HE.getOrDefault(e, e) + " ל-" + (secs / 60 >= 1 ? (secs / 60) + " דקות." : secs + " שניות."));
            }
        }
        if (wantKill) {
            for (String h : Data.HOSTILES) r.cmds.add(new Cmd("execute at " + uuid + " run kill @e[type=" + h + ",distance=..40]", null, null));
            okLines.add("ניקיתי מפלצות מסביבך.");
        }
        if (wantTp) {
            if (coords) {
                int x = Integer.parseInt(cm.group(1)), y = Math.max(-64, Math.min(320, Integer.parseInt(cm.group(2)))), z = Integer.parseInt(cm.group(3));
                r.cmds.add(new Cmd("tp " + uuid + " " + x + " " + y + " " + z, null, "לא הצלחתי לשגר אותך לשם."));
                okLines.add("שיגרתי אותך ל-" + x + " " + y + " " + z + ".");
            } else if (tpName != null) {
                r.cmds.add(new Cmd("tp " + uuid + " " + tpName, null, "לא הצלחתי לשגר אותך לשם."));
                okLines.add("שיגרתי אותך אל " + tpName + ".");
            } else if (upReq) {
                r.cmds.add(new Cmd("execute as " + uuid + " at " + uuid + " run tp @s ~ ~40 ~", null, null));
                r.cmds.add(new Cmd("effect give " + uuid + " slow_falling 60 0 true", null, null));
                okLines.add("העליתי אותך למעלה. נפילה איטית ל-60 שניות, שלא תיפגע.");
            }
        }

        // ---------------- collect replies ----------------
        if (!okLines.isEmpty()) {
            String prefix;
            if (tier == 3) prefix = pick("בשמחה, חבר!", "בכיף! הנה הטוב ביותר.", "ברור, בשבילך הכל.");
            else if (tier == 2) prefix = pick("בשמחה!", "בטח!", "כמובן!");
            else prefix = pick("אוקיי.", "בסדר.", "הנה.");
            r.say.add(prefix);
            r.say.addAll(okLines);
        }
        r.say.addAll(noteLines);

        boolean acted = !okLines.isEmpty() || !noteLines.isEmpty() || statusAsk;
        if (!acted) {
            // small talk
            if (sorry) { if (r.say.isEmpty()) r.say.add(pick("בסדר, אין בעיה.", "מסכים לסלוח.")); }
            else if (anyIn(toks, BYE)) r.say.add(pick("להתראות! אני כאן אם תצטרך.", "ביי! תחזור בקרוב."));
            else if (anyIn(toks, HOWRU)) r.say.add(pick("אני מצוין, תודה ששאלת! מה אפשר לעשות בשבילך?", "אני בסדר גמור. תבקש ממני משהו!"));
            else if (anyIn(toks, GREET)) {
                if (tier == 3) r.say.add(pick("היי חבר! מה נעשה היום?", "שלום שלום! מוכן לכל דבר."));
                else r.say.add(pick("שלום! במה אפשר לעזור?", "היי! תגיד מה אתה צריך.", "אהלן! אני מקשיב."));
            }
            else if (anyIn(toks, THANKS)) r.say.add(pick("בכיף!", "בשמחה, זה התפקיד שלי.", "אין בעד מה!"));
            else if (rude == 0 && (d > 0)) r.say.add(pick("תודה על הנימוס!", "איזה כיף לדבר איתך."));
            else if (rude == 0) r.say.add(pick("לא הבנתי את זה. נסה לנסח אחרת, או כתוב 'עוזר עזרה'.",
                    "הממ, זה לא משהו שאני מכיר. כתוב 'עוזר עזרה' כדי לראות מה אני יודע."));
        }
        return r;
    }

    // =====================================================================================
    private static boolean anyIn(List<String> toks, Set<String> s) { for (String w : toks) if (Hebrew.in(s, w)) return true; return false; }
    private static boolean exact(List<String> toks, Set<String> s) { for (String w : toks) if (s.contains(w)) return true; return false; }

    private static String hebStruct(String s) {
        switch (s) {
            case "house": return "בית"; case "tower": return "מגדל"; case "platform": return "פלטפורמה";
            case "wall": return "קיר"; case "pyramid": return "פירמידה"; case "pool": return "בריכה"; case "castle": return "טירה";
            default: return s;
        }
    }
    private static String defaultMat(String s) {
        switch (s) {
            case "house": return "oak_planks"; case "pyramid": return "sandstone"; case "platform": return "stone_bricks";
            default: return "stone_bricks";
        }
    }

    // ---------------- scanning ----------------
    static final class Thing {
        Data.Item item; Data.Mob mob; String piece; String rawId; int qty = -1; boolean rawMob; String word; int idx = -1;
    }
    static final class EnchReq { final String id; final int level; EnchReq(String id, int level) { this.id = id; this.level = level; } }
    static final class Scan {
        final List<Thing> things = new ArrayList<>();
        final List<EnchReq> enchs = new ArrayList<>();
        final List<String> mats = new ArrayList<>();
        final List<Integer> matIdx = new ArrayList<>();
        boolean best;
    }

    private Scan scan(List<String> toks) {
        Scan sc = new Scan();
        boolean summonVerb = anyIn(toks, SUMMON_V);
        int pendVal = -1, pendIdx = -10;
        for (int i = 0; i < toks.size(); i++) {
            String t = toks.get(i);
            int[] n = Hebrew.number(toks, i);
            if (n != null) { if (n[0] > 0) { pendVal = n[0]; pendIdx = i + n[1] - 1; } i += n[1] - 1; continue; }
            if (Hebrew.in(BEST_W, t)) { sc.best = true; continue; }
            String eid = Hebrew.find(Data.ENCH, t);
            if (eid != null) {
                int lvl = -1, j = i + 1;
                if (j < toks.size() && Hebrew.in(LEVEL_W, toks.get(j))) j++;
                if (j < toks.size()) { int[] m = Hebrew.number(toks, j); if (m != null && m[0] > 0) { lvl = m[0]; i = j + m[1] - 1; } }
                sc.enchs.add(new EnchReq(eid, lvl));
                continue;
            }
            String pk = Hebrew.find(Data.PIECES, t);
            if (pk != null) {
                Thing th = new Thing(); th.piece = pk; th.idx = i;
                if (i - pendIdx <= 3 && pendVal > 0) { th.qty = pendVal; pendVal = -1; }
                sc.things.add(th); continue;
            }
            String mk = Hebrew.find(Data.MATERIALS, t);
            if (mk != null) { sc.mats.add(mk); sc.matIdx.add(i); }
            Data.Mob mb = Hebrew.find(Data.MOBS, t);
            Data.Item it = Hebrew.find(Data.ITEMS, t);
            if (mb != null && it == null) {
                Thing th = new Thing(); th.mob = mb;
                if (i - pendIdx <= 3 && pendVal > 0) { th.qty = pendVal; pendVal = -1; }
                sc.things.add(th); continue;
            }
            if (it != null) {
                Thing th = new Thing(); th.item = it; th.word = t;
                if (i - pendIdx <= 3 && pendVal > 0) { th.qty = pendVal; pendVal = -1; }
                sc.things.add(th); continue;
            }
            if (t.indexOf('_') > 0 && t.matches("[a-z0-9_]+") && t.length() >= 5) {
                Thing th = new Thing(); th.rawId = t; th.rawMob = summonVerb;
                if (i - pendIdx <= 3 && pendVal > 0) { th.qty = pendVal; pendVal = -1; }
                sc.things.add(th);
            }
        }
        return sc;
    }

    private static String armorMat(String m) { return m.equals("wooden") ? "leather" : m.equals("stone") ? "chainmail" : m; }
    private static String toolMat(String m) { return m.equals("leather") ? "wooden" : m.equals("chainmail") ? "stone" : m; }
    private static String matHe(String m) {
        switch (m) {
            case "netherite": return "נתרייט"; case "diamond": return "יהלום"; case "iron": return "ברזל"; case "golden": return "זהב";
            case "stone": return "אבן"; case "wooden": return "עץ"; case "leather": return "עור"; case "chainmail": return "שרשרת"; case "copper": return "נחושת";
            default: return m;
        }
    }
    private static String pieceHe(String p) {
        switch (p) {
            case "helmet": return "קסדה"; case "chestplate": return "חולצת שריון"; case "leggings": return "מכנסי שריון"; case "boots": return "מגפיים";
            case "sword": return "חרב"; case "axe": return "גרזן"; case "pickaxe": return "מכוש"; case "shovel": return "אתי"; case "hoe": return "מעדר"; case "spear": return "חנית";
            default: return p;
        }
    }

    private static String nearestMat(Scan sc, int pieceIdx) {
        int best = -1, bestD = 99;
        for (int k = 0; k < sc.matIdx.size(); k++) {
            int d = sc.matIdx.get(k) - pieceIdx;
            int ad = Math.abs(d);
            if (ad <= 2 && (ad < bestD || (ad == bestD && d > 0))) { best = k; bestD = ad; }
        }
        return best < 0 ? null : sc.mats.get(best);
    }

    private static final class Spec { String id; int qty; String he; Spec(String id, int qty, String he) { this.id = id; this.qty = qty; this.he = he; } }

    private void giveAndSummon(Reply r, String uuid, Scan sc, int tier, List<String> toks, List<String> ok, List<String> notes) {
        boolean hasPiece = false;
        for (Thing t : sc.things) if (t.piece != null) hasPiece = true;
        int allowedMat = Math.max(0, tier - 1);

        String downMat = null;

        List<Spec> specs = new ArrayList<>();
        List<String> refused = new ArrayList<>();
        List<String> capNotes = new ArrayList<>();
        int itemCap = tier == 1 ? 16 : tier == 2 ? 64 : 640;
        int gearCap = tier == 1 ? 2 : tier == 2 ? 5 : 36;
        int mobCap = tier == 1 ? 3 : tier == 2 ? 10 : 30;

        for (Thing t : sc.things) {
            if (t.piece != null) {
                List<String> ps = new ArrayList<>();
                switch (t.piece) {
                    case "armor": ps = Arrays.asList("helmet", "chestplate", "leggings", "boots"); break;
                    case "tools": ps = Arrays.asList("sword", "pickaxe", "axe", "shovel"); break;
                    default: ps = Collections.singletonList(t.piece);
                }
                String mat = nearestMat(sc, t.idx);
                if (mat == null && !sc.mats.isEmpty()) mat = sc.mats.get(0);
                if (mat == null) mat = allowedMat >= 2 ? "netherite" : allowedMat == 1 ? "diamond" : "iron";
                int mt = mat.equals("netherite") ? 2 : mat.equals("diamond") ? 1 : 0;
                if (mt > allowedMat) { mat = allowedMat >= 1 ? "diamond" : "iron"; downMat = mat; }
                for (String p : ps) {
                    boolean arm = p.equals("helmet") || p.equals("chestplate") || p.equals("leggings") || p.equals("boots");
                    String m = arm ? armorMat(mat) : toolMat(mat);
                    String id = m + "_" + p;
                    int q = t.qty < 0 ? 1 : t.qty;
                    if (q > gearCap) { q = gearCap; capNotes.add("ציוד: עד " + gearCap + " מכל סוג."); }
                    specs.add(new Spec(id, q, pieceHe(p) + " " + matHe(m)));
                }
            } else if (t.item != null) {
                if (hasPiece && t.item.materialWord) continue; // "diamond" in "diamond sword" is the material
                int need = t.item.tier == 0 ? 1 : t.item.tier == 1 ? 2 : 3;
                if (tier < need) { refused.add(t.item.he); continue; }
                int q = t.qty < 0 ? 1 : t.qty;
                boolean gear = Data.category(t.item.id) != null;
                int cap = gear ? gearCap : itemCap;
                if (q > cap) { q = cap; capNotes.add("עד " + cap + " בכל פעם."); }
                specs.add(new Spec(t.item.id, q, t.item.he));
            } else if (t.rawId != null && !t.rawMob) {
                int need = Data.RAW_BAD.containsKey(t.rawId) ? 3 : 2;
                if (tier < need) { refused.add(t.rawId); continue; }
                int q = t.qty < 0 ? 1 : t.qty;
                if (q > itemCap) { q = itemCap; capNotes.add("עד " + itemCap + " בכל פעם."); }
                specs.add(new Spec(t.rawId, q, t.rawId));
            }
        }

        // --- enchantments ---
        boolean wantEnch = sc.best || !sc.enchs.isEmpty();
        Set<String> cantEnch = new LinkedHashSet<>();
        Set<String> notFit = new LinkedHashSet<>();
        Set<String> appliedIds = new HashSet<>();
        boolean clamped = false; int capShown = 0;
        List<String> lines = new ArrayList<>();
        for (Spec s : specs) {
            String cat = Data.category(s.id);
            Map<String, Integer> en = new LinkedHashMap<>();
            if (wantEnch) {
                if (cat == null) { cantEnch.add(s.he); }
                else {
                    if (sc.best) for (String e : Data.BEST.get(cat)) {
                        String[] a = e.split(":"); int mx = Data.ENCH_MAX.get(a[0]);
                        int cap = tier == 1 ? Math.min(2, mx) : mx;
                        en.put(a[0], Math.min(Integer.parseInt(a[1]), cap));
                    }
                    for (EnchReq q : sc.enchs) {
                        if (!Data.ENCH_OK.get(cat).contains(q.id)) { notFit.add(q.id); continue; }
                        appliedIds.add(q.id);
                        int mx = Data.ENCH_MAX.get(q.id);
                        int cap = tier == 1 ? Math.min(2, mx) : tier == 2 ? mx : 255;
                        int want = q.level < 0 ? mx : q.level;
                        int lv = Math.min(Math.max(1, want), cap);
                        if (lv < want) { clamped = true; capShown = Math.max(capShown, cap); }
                        en.put(q.id, lv);
                    }
                }
            }
            String comp = "";
            if (!en.isEmpty()) {
                StringBuilder b = new StringBuilder("[enchantments={");
                boolean first = true;
                for (Map.Entry<String, Integer> e : en.entrySet()) { if (!first) b.append(','); first = false; b.append(e.getKey()).append(':').append(e.getValue()); }
                comp = b.append("}]").toString();
            }
            String plain = "give " + uuid + " " + s.id + " " + s.qty;
            String full = "give " + uuid + " " + s.id + comp + " " + s.qty;
            r.cmds.add(new Cmd(full, comp.isEmpty() ? null : plain, "לא מצאתי פריט כזה: " + s.id));
            StringBuilder ln = new StringBuilder();
            ln.append(s.qty).append(" × ").append(s.he);
            if (!en.isEmpty()) {
                ln.append(" (");
                boolean first = true;
                for (Map.Entry<String, Integer> e : en.entrySet()) { if (!first) ln.append(", "); first = false; ln.append(Data.ENCH_HE.getOrDefault(e.getKey(), e.getKey())).append(' ').append(e.getValue()); }
                ln.append(")");
            }
            lines.add(ln.toString());
        }
        if (!lines.isEmpty()) ok.add("קיבלת: " + String.join("; ", lines) + ".");
        if (downMat != null) notes.add("חומר כזה יקר שמור לחברים טובים באמת, אז נתתי " + matHe(downMat) + ".");
        if (clamped) notes.add("את הרמה הזו אני נותן רק עד " + capShown + ". תהיה יותר נחמד אלי ונראה...");
        for (String c : new LinkedHashSet<>(capNotes)) notes.add(c);
        if (!cantEnch.isEmpty()) notes.add("אי אפשר לקסם: " + String.join(", ", cantEnch) + ".");
        List<String> nf = new ArrayList<>();
        for (String id : notFit) if (!appliedIds.contains(id)) nf.add(Data.ENCH_HE.getOrDefault(id, id));
        if (!nf.isEmpty()) notes.add("הקסם לא מתאים לפריט: " + String.join(", ", nf) + ".");
        if (!refused.isEmpty()) notes.add(pick("את " + String.join(", ", refused) + " אני נותן רק לחברים טובים. תגיד 'בבקשה' ו'תודה' ונראה.",
                String.join(", ", refused) + "? תתנהג יפה קודם."));

        // --- mobs ---
        for (Thing t : sc.things) {
            Data.Mob m = t.mob;
            String id; String he; int mt;
            if (m != null) { id = m.id; he = m.he; mt = m.tier; }
            else if (t.rawId != null && t.rawMob) { id = t.rawId; he = t.rawId; mt = 2; }
            else continue;
            int need = mt == 0 ? 1 : mt == 1 ? 2 : 3;
            if (tier < need) { notes.add("את " + he + " אני מזמן רק לחברים טובים." + (mt == 2 ? " זה כבר ממש מסוכן." : "")); continue; }
            int q = t.qty < 0 ? 1 : Math.min(t.qty, mobCap);
            if (t.qty > mobCap) notes.add("עד " + mobCap + " יצורים בכל פעם.");
            boolean light = id.equals("lightning_bolt") || id.equals("tnt");
            if (light) q = Math.min(q, 5);
            for (int k = 0; k < q; k++) {
                int dx = rnd.nextInt(5) - 2, dz = rnd.nextInt(5) - 2;
                r.cmds.add(new Cmd("execute at " + uuid + " rotated ~ 0 positioned ^ ^ ^5 run summon " + id + " ~" + dx + " ~ ~" + dz, null, k == 0 ? "לא הצלחתי לזמן: " + id : null));
            }
            ok.add("זימנתי " + q + " × " + he + ".");
        }
    }

    // ---------------- building ----------------
    private static String rel(int v) { return v == 0 ? "~" : "~" + v; }
    private static String box(int x1, int y1, int z1, int x2, int y2, int z2) {
        return rel(x1) + " " + rel(y1) + " " + rel(z1) + " " + rel(x2) + " " + rel(y2) + " " + rel(z2);
    }

    private void buildCmds(Reply r, String uuid, String struct, String m, int tier) {
        int dist;
        List<String> c = new ArrayList<>();
        switch (struct) {
            case "house":
                dist = 8;
                c.add("fill " + box(-3, -1, -3, 3, 4, 3) + " " + m + " hollow");
                c.add("fill " + box(-3, 0, 0, -3, 1, 0) + " air");
                c.add("fill " + box(3, 0, 0, 3, 1, 0) + " air");
                c.add("fill " + box(0, 0, -3, 0, 1, -3) + " air");
                c.add("fill " + box(0, 0, 3, 0, 1, 3) + " air");
                for (int s : new int[]{-2, 2}) {
                    c.add("setblock " + rel(-3) + " ~2 " + rel(s) + " glass");
                    c.add("setblock " + rel(3) + " ~2 " + rel(s) + " glass");
                    c.add("setblock " + rel(s) + " ~2 " + rel(-3) + " glass");
                    c.add("setblock " + rel(s) + " ~2 " + rel(3) + " glass");
                }
                c.add("setblock ~ ~3 ~ lantern[hanging=true]");
                c.add("setblock ~2 ~ ~2 crafting_table");
                c.add("setblock ~-2 ~ ~2 furnace");
                c.add("setblock ~2 ~ ~-2 chest");
                break;
            case "tower":
                dist = 8;
                c.add("fill " + box(-2, -1, -2, 2, 14, 2) + " " + m + " hollow");
                c.add("fill " + box(-2, 0, 0, -2, 1, 0) + " air");
                c.add("fill " + box(2, 0, 0, 2, 1, 0) + " air");
                c.add("fill " + box(0, 0, 0, 0, 13, 0) + " scaffolding");
                c.add("setblock ~ ~14 ~ air");
                for (int h : new int[]{6, 11}) {
                    c.add("setblock ~-2 ~" + h + " ~ glass"); c.add("setblock ~2 ~" + h + " ~ glass");
                    c.add("setblock ~ ~" + h + " ~-2 glass"); c.add("setblock ~ ~" + h + " ~2 glass");
                }
                for (int sx : new int[]{-2, 2}) for (int sz : new int[]{-2, 2}) c.add("setblock " + rel(sx) + " ~15 " + rel(sz) + " " + m);
                break;
            case "platform":
                dist = 9;
                c.add("fill " + box(-7, -1, -7, 7, -1, 7) + " " + m);
                break;
            case "wall":
                dist = 6;
                c.add("fill " + box(-7, 0, 0, 7, 4, 0) + " " + m);
                break;
            case "pyramid": {
                dist = 14;
                int n = tier >= 3 ? 12 : 8;
                for (int k = 0; k < n; k++) { int h = n - 1 - k; c.add("fill " + box(-h, k - 1, -h, h, k - 1, h) + " " + m); }
                break;
            }
            case "pool":
                dist = 6;
                c.add("fill " + box(-3, -3, -3, 3, -1, 3) + " water");
                break;
            default: { // castle
                dist = 15;
                c.add("fill " + box(-8, -1, -8, 8, 6, 8) + " " + m + " hollow");
                c.add("fill " + box(-8, 0, -1, -8, 3, 1) + " air");
                c.add("fill " + box(8, 0, -1, 8, 3, 1) + " air");
                c.add("fill " + box(-1, 0, -8, 1, 3, -8) + " air");
                c.add("fill " + box(-1, 0, 8, 1, 3, 8) + " air");
                for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
                    c.add("fill " + box(sx * 8 - 2, -1, sz * 8 - 2, sx * 8 + 2, 11, sz * 8 + 2) + " " + m + " hollow");
                    c.add("setblock " + rel(sx * 8) + " ~12 " + rel(sz * 8) + " " + m);
                }
                break;
            }
        }
        String pre = "execute at " + uuid + " rotated ~ 0 positioned ^ ^ ^" + dist + " run ";
        for (String s : c) r.cmds.add(new Cmd(pre + s, null, null));
    }
}

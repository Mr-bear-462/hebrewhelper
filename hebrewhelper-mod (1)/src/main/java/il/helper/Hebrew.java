package il.helper;

import java.util.*;

/** Hebrew text helpers: normalisation, tokenising, prefix stripping, number words. */
final class Hebrew {
    private Hebrew() {}

    /** Lowercase, strip niqqud/punctuation, unify Hebrew final letters. Keeps letters, digits, underscore. */
    static String key(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c >= 0x0591 && c <= 0x05C7) continue; // niqqud, cantillation
            switch (c) {
                case '\u05DA': c = '\u05DB'; break; // ך -> כ
                case '\u05DD': c = '\u05DE'; break; // ם -> מ
                case '\u05DF': c = '\u05E0'; break; // ן -> נ
                case '\u05E3': c = '\u05E4'; break; // ף -> פ
                case '\u05E5': c = '\u05E6'; break; // ץ -> צ
                default: break;
            }
            if (c == '\'' || c == '"' || c == '\u05F3' || c == '\u05F4' || c == '\u2019' || c == '`') continue;
            if (Character.isLetterOrDigit(c) || c == '_') b.append(c); else b.append(' ');
        }
        return b.toString().trim().replaceAll("\\s+", " ");
    }

    /** Normalise, apply multi-word phrases (joined with underscore) and split to tokens. */
    static List<String> tokens(String text, Map<String, String> phrases) {
        String t = " " + key(text) + " ";
        List<Map.Entry<String, String>> ordered = new ArrayList<>(phrases.entrySet());
        ordered.sort((a, b) -> b.getKey().length() - a.getKey().length()); // longest phrase first
        for (int pass = 0; pass < 2; pass++) {
            for (Map.Entry<String, String> e : ordered) {
                String p = e.getKey();
                t = t.replace(" " + p + " ", " " + e.getValue() + " ");
                if (p.charAt(0) >= '\u05D0' && p.charAt(0) <= '\u05EA') {
                    for (int i = 0; i < PREFIXES.length(); i++) t = t.replace(" " + PREFIXES.charAt(i) + p + " ", " " + e.getValue() + " ");
                }
            }
        }
        t = t.trim();
        if (t.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(t.split(" ")));
    }

    private static final String PREFIXES = "\u05D1\u05DC\u05D4\u05D5\u05DB\u05DE\u05E9"; // ב ל ה ו כ מ ש

    /** Dictionary lookup that also tries removing up to 3 Hebrew prefix letters. */
    private static boolean canStrip(String s) {
        char c = s.charAt(0);
        if (PREFIXES.indexOf(c) < 0) return false;
        int min = (c == '\u05DE' || c == '\u05DB' || c == '\u05E9') ? 4 : 3; // מ כ ש are part of real words more often
        return s.length() >= min;
    }

    static <T> T find(Map<String, T> m, String w) {
        T v = m.get(w);
        if (v != null) return v;
        String s = w;
        for (int k = 0; k < 3 && canStrip(s); k++) {
            s = s.substring(1);
            v = m.get(s);
            if (v != null) return v;
        }
        return null;
    }

    static boolean in(Set<String> set, String w) {
        if (set.contains(w)) return true;
        String s = w;
        for (int k = 0; k < 3 && canStrip(s); k++) {
            s = s.substring(1);
            if (set.contains(s)) return true;
        }
        return false;
    }

    /** Normalised set used for exact (no prefix stripping) matching. */
    static Set<String> exactSet(String... words) {
        Set<String> s = new HashSet<>();
        for (String w : words) s.add(key(w).replace(' ', '_'));
        return s;
    }

    static Set<String> set(String... words) {
        Set<String> s = new HashSet<>();
        for (String w : words) s.add(key(w).replace(' ', '_'));
        return s;
    }

    // ---------- numbers ----------
    private static final Map<String, Integer> UNITS = new HashMap<>();
    private static final Map<String, Integer> TENS = new HashMap<>();
    private static final Set<String> TEENS = new HashSet<>();

    private static void u(int v, String... ws) { for (String w : ws) UNITS.put(key(w), v); }
    private static void t(int v, String... ws) { for (String w : ws) TENS.put(key(w), v); }

    static {
        u(0, "אפס");
        u(1, "אחד", "אחת");
        u(2, "שניים", "שנים", "שתיים", "שתים", "שני", "שתי");
        u(3, "שלוש", "שלושה");
        u(4, "ארבע", "ארבעה");
        u(5, "חמש", "חמישה", "חמשה");
        u(6, "שש", "שישה");
        u(7, "שבע", "שבעה");
        u(8, "שמונה");
        u(9, "תשע", "תשעה");
        u(10, "עשר", "עשרה");
        t(20, "עשרים"); t(30, "שלושים"); t(40, "ארבעים"); t(50, "חמישים");
        t(60, "שישים"); t(70, "שבעים"); t(80, "שמונים"); t(90, "תשעים");
        t(100, "מאה"); t(200, "מאתיים");
        TEENS.add(key("עשר")); TEENS.add(key("עשרה"));
    }

    private static Integer digits(String w) {
        String s = w;
        while (s.startsWith("x")) s = s.substring(1);
        while (s.endsWith("x")) s = s.substring(0, s.length() - 1);
        if (s.isEmpty() || s.length() > 4) return null;
        for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    /** Try to read a number at token i. Returns {value, tokensUsed} or null. */
    static int[] number(List<String> t, int i) {
        String w = t.get(i);
        Integer d = digits(w);
        if (d != null) return new int[]{d, 1};
        Integer ten = TENS.get(w);
        if (ten != null) {
            if (ten < 100 && i + 1 < t.size()) {
                String n = t.get(i + 1);
                if (n.length() > 1 && n.charAt(0) == '\u05D5') { // ו
                    Integer un = UNITS.get(n.substring(1));
                    if (un != null && un >= 1 && un < 10) return new int[]{ten + un, 2};
                }
            }
            return new int[]{ten, 1};
        }
        Integer un = UNITS.get(w);
        if (un != null) {
            if (un >= 1 && un < 10 && i + 1 < t.size() && TEENS.contains(t.get(i + 1))) return new int[]{un + 10, 2};
            return new int[]{un, 1};
        }
        return null;
    }
}

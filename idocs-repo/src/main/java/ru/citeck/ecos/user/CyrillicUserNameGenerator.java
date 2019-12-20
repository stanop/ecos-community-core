package ru.citeck.ecos.user;

import org.alfresco.service.cmr.security.PersonService;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class CyrillicUserNameGenerator {

    private Map<String, String> translit = createTranslitMap();


    /**
     * name generator pattern
     */
    private String namePattern = "%fI%%mI%.%lastName%";

    private PersonService personService;

    /**
     * The pattern of the user name to generate
     * <p>
     * Patterns available:
     * %lastName%,  lower case last name
     * %firstName%, lower case first name
     * %middleName%, lower case middle name
     * %fI% lower case first name initial
     * %lI% lower case last name initial
     * %mI% lower case middle name initial
     *
     * @param userNamePattern String
     */
    public void setNamePattern(String userNamePattern) {
        this.namePattern = userNamePattern;
    }

    private String generateUserName(String lastName, String firstName, String middleName, int seed) {
        String userName;

        String pattern = namePattern;

        userName = pattern
                .replace("%firstName%", firstName)
                .replace("%fI%", getInitial(firstName))
                .replace("%middleName%", middleName)
                .replace("%mI%", getInitial(middleName))
                .replace("%lastName%", lastName)
                .replace("%lI%", getInitial(lastName));

        if (seed > 1) {
            userName = userName + seed;
        }

        return cyrillicToLatin(userName);
    }

    public String getUserName(String lastName, String firstName, String middleName) {

        if (("".equals(lastName) || null == lastName)
                && ("".equals(firstName) || null == firstName)
                && ("".equals(middleName) || null == middleName)) {
            throw new IllegalArgumentException("FIO is empty");
        }

        String userName;
        int i = 1;
        do {
            userName = generateUserName(checkNull(lastName), checkNull(firstName), checkNull(middleName), i);
            i++;
        } while (personService.personExists(userName));
        return userName;
    }

    private String cyrillicToLatin(String name) {
        StringBuilder result = new StringBuilder();

        // Replace whitespace with _
        String cleanString = name.trim().toLowerCase().replaceAll("\\s+", "_");

        for (int i = 0; i < cleanString.length(); i++) {
            char currentChar = cleanString.charAt(i);
            String charWrapper = Character.toString(currentChar);
            result.append(translit.getOrDefault(charWrapper, charWrapper));
        }

        return Normalizer.normalize(result.toString(), Normalizer.Form.NFD).replaceAll("[^a-zA-z0-9_.]+", "");
    }

    private Map<String, String> createTranslitMap() {
        Map<String, String> translitMap = new HashMap<>();
        translitMap.put("\u042f", "Ya");
        translitMap.put("\u044f", "ya");
        translitMap.put("\u042e", "Yu");
        translitMap.put("\u044e", "yu");
        translitMap.put("\u0427", "Ch");
        translitMap.put("\u0447", "ch");
        translitMap.put("\u0428", "Sh");
        translitMap.put("\u0448", "sh");
        translitMap.put("\u0429", "Sh");
        translitMap.put("\u0449", "sh");
        translitMap.put("\u0416", "Zh");
        translitMap.put("\u0436", "zh");
        translitMap.put("\u0410", "A");
        translitMap.put("\u0430", "a");
        translitMap.put("\u0411", "B");
        translitMap.put("\u0431", "b");
        translitMap.put("\u0412", "V");
        translitMap.put("\u0432", "v");
        translitMap.put("\u0413", "G");
        translitMap.put("\u0433", "g");
        translitMap.put("\u0414", "D");
        translitMap.put("\u0434", "d");
        translitMap.put("\u0415", "E");
        translitMap.put("\u0435", "e");
        translitMap.put("\u0401", "E");
        translitMap.put("\u0451", "e");
        translitMap.put("\u0417", "Z");
        translitMap.put("\u0437", "z");
        translitMap.put("\u0418", "I");
        translitMap.put("\u0438", "i");
        translitMap.put("\u0419", "J");
        translitMap.put("\u0439", "j");
        translitMap.put("\u041a", "K");
        translitMap.put("\u043a", "k");
        translitMap.put("\u041b", "L");
        translitMap.put("\u043b", "l");
        translitMap.put("\u041c", "M");
        translitMap.put("\u043c", "m");
        translitMap.put("\u041d", "N");
        translitMap.put("\u043d", "n");
        translitMap.put("\u041e", "O");
        translitMap.put("\u043e", "o");
        translitMap.put("\u041f", "P");
        translitMap.put("\u043f", "p");
        translitMap.put("\u0420", "R");
        translitMap.put("\u0440", "r");
        translitMap.put("\u0421", "S");
        translitMap.put("\u0441", "s");
        translitMap.put("\u0422", "T");
        translitMap.put("\u0442", "t");
        translitMap.put("\u0423", "U");
        translitMap.put("\u0443", "u");
        translitMap.put("\u0424", "F");
        translitMap.put("\u0444", "f");
        translitMap.put("\u0425", "H");
        translitMap.put("\u0445", "h");
        translitMap.put("\u0426", "C");
        translitMap.put("\u0446", "c");
        translitMap.put("\u042b", "Y");
        translitMap.put("\u044b", "y");
        translitMap.put("\u042c", "");
        translitMap.put("\u044c", "");
        translitMap.put("\u042a", "");
        translitMap.put("\u044a", "");
        translitMap.put("\u042d", "E");
        translitMap.put("\u044d", "e");
        return translitMap;
    }

    private String checkNull(String name) {
        if (name == null) {
            return "";
        } else {
            return name;
        }
    }

    private String getInitial(String name) {
        if (name.isEmpty()) {
            return "";
        } else {
            return name.toLowerCase().substring(0, 1);
        }
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}

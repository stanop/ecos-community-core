package ru.citeck.ecos.dto;

/**
 * Mail case model data transfer object
 */
public class MailDto extends ActionDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "mail";

    /**
     * Mail to
     */
    private String mailTo;

    /**
     * To many
     */
    private String toMany;

    /**
     * Subject
     */
    private String subject;

    /**
     * From user
     */
    private String fromUser;

    /**
     * Mail text
     */
    private String mailText;

    /**
     * Process variable value
     */
    private String mailHtml;

    /** Getters and setters */

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getToMany() {
        return toMany;
    }

    public void setToMany(String toMany) {
        this.toMany = toMany;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getMailText() {
        return mailText;
    }

    public void setMailText(String mailText) {
        this.mailText = mailText;
    }

    public String getMailHtml() {
        return mailHtml;
    }

    public void setMailHtml(String mailHtml) {
        this.mailHtml = mailHtml;
    }

}

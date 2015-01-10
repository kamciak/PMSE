package com.publicationmetasearchengine.services.mailingservice.emailparts;

public class PasswordRecoveryEmailBody implements EmailBody {
    private String userRealName;
    private String username;
    private String password;

    public PasswordRecoveryEmailBody(String userRealName, String username, String password) {
        this.userRealName = userRealName;
        this.username = username;
        this.password = password;
    }

    @Override
    public String getBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(userRealName == null? "user" : userRealName).append(",\n")
                .append("If you forgot your login information, here are your credentials:\n")
                .append("\tLogin: ").append(username).append("\n")
                .append("\tPassword: ").append(password).append("\n\n")
                .append("Regards,\n")
                .append("\t\tPublicationMetaSearchEngine Team");
        return sb.toString();
    }
}

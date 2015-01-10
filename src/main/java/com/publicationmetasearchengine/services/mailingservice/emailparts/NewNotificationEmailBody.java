package com.publicationmetasearchengine.services.mailingservice.emailparts;

import java.util.Set;

public class NewNotificationEmailBody implements EmailBody {
    private final String userRealName;
    private final String username;
    private final String criteriaTitle;
    private final Set<String> titles;

    public NewNotificationEmailBody(String userRealName, String username, String criteriaTitle, Set<String> titles) {
        this.userRealName = userRealName;
        this.username = username;
        this.criteriaTitle = criteriaTitle;
        this.titles = titles;
    }

    @Override
    public String getBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(userRealName == null? username : userRealName).append(",\n")
          .append("There are some new publications matching ").append(criteriaTitle).append(" search criterias:\n");
        for(String title: titles)
            sb.append("\t\t- ").append(title).append("\n");
        sb.append("You can find out more, by visitting your To-Read section.\n\n")
          .append("Regards,\n")
          .append("\t\tPublicationMetaSearchEngine Team");
        return sb.toString();
    }
}

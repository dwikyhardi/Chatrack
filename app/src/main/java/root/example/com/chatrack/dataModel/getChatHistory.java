package root.example.com.chatrack.dataModel;

import com.google.firebase.database.DataSnapshot;

public class getChatHistory {
    private String chatDate;
    private String chatMessage;
    private String chatTime;
    private String UserId;
    private String chatUserName;

    public getChatHistory(String chatDate,
                          String chatMessage,
                          String chatTime,
                          String UserId,
                          String chatUserName) {
        this.chatDate = chatDate;
        this.chatMessage = chatMessage;
        this.chatTime = chatTime;
        this.UserId = UserId;
        this.chatUserName = chatUserName;
    }

    public String getChatDate() {
        return chatDate;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public String getChatTime() {
        return chatTime;
    }

    public String getUserId() {
        return UserId;
    }

    public String getChatUserName() {
        return chatUserName;
    }
}

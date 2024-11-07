package com.example.kradankanban_backend.common.services;
import com.example.kradankanban_backend.common.entities.CollabEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class SendEmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String inviteName, String email,String boardName, CollabEntity.AccessRight accessRight, String boardId) throws MessagingException, UnsupportedEncodingException {
        String subject = inviteName + " has invited you to be collaborator";
        String acceptedPath = "http://localhost:5173/board/" + boardId + "/collab/invitations";
        String content = "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; background-color: #ffffff; margin: 0; padding: 0; }"
                + ".container { width: 100%; max-width: 600px; margin: 20px auto; background-color: #ffffff; border: 1px solid #ddd; border-radius: 8px; padding: 20px; }"
                + "h2 { color: #333; }"
                + "p { color: #555; line-height: 1.6; }"
                + "a { color: #4CAF50; text-decoration: none; }"
                + ".button { display: inline-block; padding: 10px 15px; background-color: #4CAF50; color: #ffffff; text-align: center; border-radius: 5px; text-decoration: none; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<h2>Hello,</h2>"
                + "<p>" + inviteName + " has invited you to collaborate on the board <strong>" + boardName + "</strong>.</p>"
                + "<p><strong>Access Rights:</strong> " + accessRight + "</p>"
                + "<p>To access the board, please click the button below:</p>"
                + "<a href='"+acceptedPath+"'>Click to Accept Invitation.</a>"
                + "<p>Thank you!<br>KP2 Team</p>"
                + "</div>"
                + "</body>"
                + "</html>";


        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom("no-reply@itbkk.kp2", "Do not reply");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }
}


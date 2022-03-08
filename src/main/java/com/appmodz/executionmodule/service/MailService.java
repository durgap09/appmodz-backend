package com.appmodz.executionmodule.service;

import java.io.IOException;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.appmodz.executionmodule.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailService {

    @Autowired
    private Environment env;

    @Autowired
    private UtilService utilService;

    public void sendRegistrationMail(String toEmail,String token) {
            // Replace sender@example.com with your "From" address.
            // This address must be verified with Amazon SES.
            String FROM = env.getProperty("SES_MAIL_SENDER");

            // Replace recipient@example.com with a "To" address. If your account
            // is still in the sandbox, this address must be verified.
            String TO = toEmail;

            String link = env.getProperty("CHANGE_PASSWORD_LINK")+token;

            // The subject line for the email.
            String SUBJECT = "Q-Cloud registration";

            // The HTML body for the email.
            String HTMLBODY = "<p>Hello please use the link below to setup your password</p><br/>"
                    + "<a href='"+link+"'>Registration Link</a><br/><br/>" +
                    "<p>Once the password is setup, you can use the information below to login to Q-Cloud.</p><br/><br/>"+
                    "<p>Registered Name: "+toEmail+"</p><br/>"+
                    "<p>Link for accessing Q-Cloud post registration: "+env.getProperty("LOGIN_LINK")+"</p><br/>";

             AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                                    env.getProperty("SES_ACCESS_KEY"),
                                    env.getProperty("SES_SECRET_KEY"))))
                            .withRegion(env.getProperty("SES_REGION")).build();
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(TO))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(HTMLBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM);
            client.sendEmail(request);
    }


    public void sendForgotPasswordMail(String toEmail,String token) {
        // Replace sender@example.com with your "From" address.
        // This address must be verified with Amazon SES.
        String FROM = env.getProperty("SES_MAIL_SENDER");

        // Replace recipient@example.com with a "To" address. If your account
        // is still in the sandbox, this address must be verified.
        String TO = toEmail;

        String link = env.getProperty("FORGOT_PASSWORD_LINK") + token;

        // The subject line for the email.
        String SUBJECT = "Qcloud Forgot Password Email";

        // The HTML body for the email.
        String HTMLBODY = "<h1>Password Reset Link</h1>"
                + "<p>Hi please use this <a href='" + link + "'>password reset link </a> to reset your password";

        AmazonSimpleEmailService client =
                AmazonSimpleEmailServiceClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                                env.getProperty("SES_ACCESS_KEY"),
                                env.getProperty("SES_SECRET_KEY"))))
                        .withRegion(env.getProperty("SES_REGION")).build();
        SendEmailRequest request = new SendEmailRequest()
                .withDestination(
                        new Destination().withToAddresses(TO))
                .withMessage(new Message()
                        .withBody(new Body()
                                .withHtml(new Content()
                                        .withCharset("UTF-8").withData(HTMLBODY)))
                        .withSubject(new Content()
                                .withCharset("UTF-8").withData(SUBJECT)))
                .withSource(FROM);
        client.sendEmail(request);
    }

    public void sendMail(String fromEmail,String subject, String content) {
        // Replace sender@example.com with your "From" address.
        // This address must be verified with Amazon SES.
        String FROM = fromEmail;

        // Replace recipient@example.com with a "To" address. If your account
        // is still in the sandbox, this address must be verified.
        String TO = env.getProperty("SES_MAIL_SENDER");

        // The subject line for the email.
        String SUBJECT = subject;

        AmazonSimpleEmailService client =
                AmazonSimpleEmailServiceClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                                env.getProperty("SES_ACCESS_KEY"),
                                env.getProperty("SES_SECRET_KEY"))))
                        .withRegion(env.getProperty("SES_REGION")).build();
        SendEmailRequest request = new SendEmailRequest()
                .withDestination(
                        new Destination().withToAddresses(TO))
                .withMessage(new Message()
                        .withBody(new Body()
                                .withText(new Content()
                                        .withCharset("UTF-8").withData(content)))
                        .withSubject(new Content()
                                .withCharset("UTF-8").withData(SUBJECT)))
                .withSource(FROM);
        client.sendEmail(request);
        utilService.logEvents(null,log,"Sent mail to "+FROM+" with subject "+SUBJECT+" to "+TO);
    }

    public void sendMail(String fromEmail,String toEmail,String subject, String content) {
        // Replace sender@example.com with your "From" address.
        // This address must be verified with Amazon SES.
        String FROM = fromEmail;

        // Replace recipient@example.com with a "To" address. If your account
        // is still in the sandbox, this address must be verified.
        String TO = toEmail;

        // The subject line for the email.
        String SUBJECT = subject;

        AmazonSimpleEmailService client =
                AmazonSimpleEmailServiceClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                                env.getProperty("SES_ACCESS_KEY"),
                                env.getProperty("SES_SECRET_KEY"))))
                        .withRegion(env.getProperty("SES_REGION")).build();
        SendEmailRequest request = new SendEmailRequest()
                .withDestination(
                        new Destination().withToAddresses(TO))
                .withMessage(new Message()
                        .withBody(new Body()
                                .withText(new Content()
                                        .withCharset("UTF-8").withData(content)))
                        .withSubject(new Content()
                                .withCharset("UTF-8").withData(SUBJECT)))
                .withSource(FROM);
        client.sendEmail(request);
        utilService.logEvents(null,log,"Sent mail to "+FROM+" with subject "+SUBJECT+" to "+TO);
    }

    public void sendFeedbackMail(User user, String content) {
        // Replace sender@example.com with your "From" address.
        // This address must be verified with Amazon SES.
        String FROM = env.getProperty("SES_MAIL_SENDER");

        // Replace recipient@example.com with a "To" address. If your account
        // is still in the sandbox, this address must be verified.
        String TO = env.getProperty("SES_MAIL_SENDER");

        String prefix = "Username: "+user.getUserName()+"\n\nName: ";
        if(user.getUserFirstName()!=null)
            prefix=prefix+user.getUserFirstName();
        if(user.getUserLastName()!=null)
            prefix = prefix+" "+user.getUserLastName();

        content = prefix+"\n\n"+content;


        AmazonSimpleEmailService client =
                AmazonSimpleEmailServiceClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                                env.getProperty("SES_ACCESS_KEY"),
                                env.getProperty("SES_SECRET_KEY"))))
                        .withRegion(env.getProperty("SES_REGION")).build();
        SendEmailRequest request = new SendEmailRequest()
                .withDestination(
                        new Destination().withToAddresses(TO))
                .withMessage(new Message()
                        .withBody(new Body()
                                .withText(new Content()
                                        .withCharset("UTF-8").withData(content)))
                        .withSubject(new Content()
                                .withCharset("UTF-8").withData("Qcloud Feedback From "+user.getUserName())))
                .withSource(FROM);
        client.sendEmail(request);
        utilService.logEvents(null,log,"Sent mail to "+FROM+" with subject "+"Qcloud Feedback From "+user.getUserName()+" to "+TO);
    }

}

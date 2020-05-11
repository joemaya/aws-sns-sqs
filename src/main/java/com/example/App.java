package com.example;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.util.Base64;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.File;
import java.util.List;

public class App {

    public static void main(String[] args) {





//        createBucket(s3);

        try {
            createTopic();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        final CreateTopicRequest createTopicRequest = new CreateTopicRequest("MyTopic");
//        final CreateTopicResult createTopicResponse = s3.createTopic(createTopicRequest);
//
//// Print the topic ARN.
//        System.out.println("TopicArn:" + createTopicResponse.getTopicArn());
//
//// Print the request ID for the CreateTopicRequest action.
//        System.out.println("CreateTopicRequest: " + snsClient.getCachedResponseMetadata(createTopicRequest));



    }

    private static void createTopic() throws Exception {

//        AwsSessionCredentials credentials = AwsSessionCredentials.create(
//                "AKIA2PNCBMTB7VXE4ARL",
//                "bcIWKxH3vDQ3BbZNaKwDpsCmzakr4th5AVog0lpP",
//                "");

        String defaultPropertiesPath = "/Users/uidai/.aws/credentials1";

        File props = new File(defaultPropertiesPath);
        AWSCredentials credentials = new PropertiesCredentials(props);



        AmazonSNS sns = new AmazonSNSClient(credentials);
        AmazonSQS sqs = new AmazonSQSClient(credentials);


        String myTopicArn = sns.createTopic(new CreateTopicRequest("topicName")).getTopicArn();
        String myQueueUrl = sqs.createQueue(new CreateQueueRequest("queueName")).getQueueUrl();

        Topics.subscribeQueue(sns, sqs, myTopicArn, myQueueUrl);

        sns.publish(new PublishRequest(myTopicArn, "Hello SNS World").withSubject("Subject"));

        List<Message> messages = sqs.receiveMessage(new ReceiveMessageRequest(myQueueUrl)).getMessages();
        if (messages.size() > 0) {

            System.out.println(messages.get(0).getBody());

            System.out.println("Message: " + messages.get(0).getBody());
//            byte[] decodedBytes = java.util.Base64.getDecoder().decode((messages.get(0)).getBody().getBytes());
//            System.out.println("Message: " +  new String(decodedBytes));
        }

    }

    private static void createBucket() {

        AwsSessionCredentials awsCreds = AwsSessionCredentials.create(
                "AKIA2PNCBMTB7VXE4ARL",
                "bcIWKxH3vDQ3BbZNaKwDpsCmzakr4th5AVog0lpP",
                "");

        S3Client s3 = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        String bucket = "bucket" + System.currentTimeMillis();
        System.out.println(bucket);

// Create bucket
        CreateBucketRequest createBucketRequest = CreateBucketRequest
                .builder()
                .bucket(bucket)
                .createBucketConfiguration(CreateBucketConfiguration.builder()
                        .locationConstraint(Region.US_WEST_2.id())
                        .build())
                .build();
        s3.createBucket(createBucketRequest);

        s3.close();
    }
}

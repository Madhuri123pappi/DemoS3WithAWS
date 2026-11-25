# Spring Boot AWS S3 File Upload and Download

A simple Spring Boot application that uploads files to an Amazon S3 bucket and downloads them using REST APIs.

## Technology

- Java 25
- Spring Boot 4.0.0
- AWS SDK for Java 2.x
- Amazon S3

## Configuration

Add the following to `src/main/resources/application.properties`:

```properties
aws.s3.region=ap-south-2
aws.s3.bucket-name=your-bucket-name

spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

Replace `your-bucket-name` with the name of your S3 bucket. Set the region to the region where the bucket was created.

The application uses the AWS SDK `DefaultCredentialsProvider`. For local development, configure credentials with:

```bash
aws configure
```

The credentials are read from the default AWS shared credentials file (`~/.aws/credentials`). Do not commit AWS access keys or secret keys to source control.

## Required AWS permissions

The AWS identity used by the application needs permission to upload and download objects in the bucket. For example:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject"
      ],
      "Resource": "arn:aws:s3:::your-bucket-name/*"
    }
  ]
}
```

Replace `your-bucket-name` in the policy with your actual bucket name. Apply least-privilege permissions appropriate to your environment.

## Run the application

From the project root (the directory containing `pom.xml`), run:

```bash
mvn spring-boot:run
```

The application starts at:

```text
http://localhost:8080
```

To stop the application, press `Ctrl + C` in the terminal where it is running.

## API endpoints

### 1. Upload a file

| Property | Value |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8080/api/s3/upload` |
| Content-Type | `multipart/form-data` |
| Form-data key | `file` (type: File) |

**Test in Postman**

1. Select `POST` and enter `http://localhost:8080/api/s3/upload`.
2. Open **Body** and select **form-data**.
3. Add a row with key `file`.
4. Change the key type from **Text** to **File**, then select a file.
5. Click **Send**.

Example successful response:

```json
{
  "message": "File uploaded successfully",
  "s3Key": "uploads/<generated-id>/example.pdf"
}
```

The service generates a unique S3 object key. Copy the returned `s3Key`; it is required to download the same object.

### 2. Download a file

| Property | Value |
|---|---|
| Method | `GET` |
| URL | `http://localhost:8080/api/s3/download?key=<S3_OBJECT_KEY>` |
| Query parameter | `key` — the exact `s3Key` returned by the upload endpoint |

**Test in Postman**

1. Select `GET`.
2. Enter the download URL and replace `<S3_OBJECT_KEY>` with the returned key. For example:

   ```text
   http://localhost:8080/api/s3/download?key=uploads/12345/example.pdf
   ```

3. Select **Send and Download** to save the returned file.

The response is sent as an attachment using the original file name.

## Request flow

```text
Postman
  -> S3Controller
      -> S3Service
          -> AWS SDK S3Client
              -> Amazon S3 bucket
```

## Troubleshooting

### 415 Unsupported Media Type on upload

The upload endpoint requires `multipart/form-data`.

- In Postman, choose **Body → form-data**.
- Set the key to `file` and select type **File**.
- Remove any manually configured `Content-Type: application/json` header. Postman will set the multipart content type and boundary automatically.

### Access denied from S3

Check that the configured AWS identity has `s3:PutObject` for upload and `s3:GetObject` for download on the target bucket objects.

### Port 8080 is already in use

Stop the other application using port 8080, or configure this application to use another port, for example:

```properties
server.port=8081
```

## Notes

- The `key` parameter is an S3 object key, not just necessarily the visible file name. Use the exact key returned by the upload endpoint.
- This sample returns the downloaded object as a byte array, so it is intended for small-to-medium files. For very large objects, implement streaming download handling.
- For AWS deployments, prefer an IAM role (such as an EC2 instance profile or ECS task role) over long-lived static credentials.

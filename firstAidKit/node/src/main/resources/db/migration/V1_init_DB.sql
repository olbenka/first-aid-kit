CREATE TABLE app_document (
                              id SERIAL PRIMARY KEY,
                              telegramFileId VARCHAR(255),
                              docName VARCHAR(255),
                              binaryContent_id BIGINT REFERENCES binary_content(id),
                              mimeType VARCHAR(255),
                              fileSize BIGINT
);
CREATE TABLE app_photo (
                           id SERIAL PRIMARY KEY,
                           telegramFileId VARCHAR(255),
                           binaryContent_id BIGINT REFERENCES binary_content(id),
                           fileSize INTEGER
);
CREATE TABLE app_user (
                          id SERIAL PRIMARY KEY,
                          telegramUserId BIGINT,
                          firstLoginDate TIMESTAMP,
                          firstName VARCHAR(255),
                          userName VARCHAR(255),
                          email VARCHAR(255),
                          isActive BOOLEAN,
                          userState VARCHAR(255)
);
CREATE TABLE binary_content (
                                id SERIAL PRIMARY KEY,
                                fileAsArrayOfBytes BYTEA
);
CREATE TABLE medication (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(255),
                            quantity INTEGER,
                            expiryDate DATE,
                            briefInfo VARCHAR(255),
                            photo_id BIGINT REFERENCES app_photo(id),
                            document_id BIGINT REFERENCES app_document(id),
                            user_id BIGINT REFERENCES app_user(id)
);
CREATE TABLE raw_data (
                          id SERIAL PRIMARY KEY,
                          event JSONB
);

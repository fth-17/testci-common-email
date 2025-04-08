package org.apache.commons.mail;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.Disabled;


public class EmailTest {
    
    // Array of test email addresses to use in various test cases
    private static final String[] TEST_EMAILS = {
        "test@abc.com", "test@cba", "abcdefghijklmnopqrst@abcdefghijklmnopqrst.bd"
        
    };

    
    // EmailConcrete is a custom implementation of an email class for testing
    private EmailConcrete email; 
    
    // Constant for mail host property name
    private static final String MAIL_HOST = "mail.host"; 

    @Before
    public void setUpEmailTest() throws Exception {
    	
        // This method runs before each test, initializing a new EmailConcrete object
        email = new EmailConcrete();
        
    }
    
    
    @After 
    public void tearDownEmailTest() throws Exception {
    	
        // This method runs after each test.
    	
    }

    
    // Test to add BCC email addresses
    @Test
    public void testAddBcc() throws Exception {
    	
        email.addBcc(TEST_EMAILS); // Add the test email addresses to BCC
        assertEquals(3, email.getBccAddresses().size()); // Ensure all 3 addresses were added
        
    }


    @Test(expected = EmailException.class)
    public void testAddBcc_Empty() throws EmailException {
    	
        EmailConcrete obj = new EmailConcrete();
        obj.addBcc(); // Expecting an EmailException due to empty input
        
    }

    
    // Test adding a single CC email address
    @Test
    public void testAddCc() throws EmailException {
    	
        EmailConcrete obj = new EmailConcrete();
        obj.addCc("test@abc.com"); // Adding a valid CC email
        // No assertion required as we are only checking if an exception is thrown
        
    }
    

    // Test adding a Reply-To email address along with a name
    @Test
    public void testAddReplyTo_ValidEmail() throws EmailException {
    	
        email.addReplyTo("test@abc.com", "Fatima Hazime"); // Valid email and name
        
    }
    

  
    

    // Test adding a header with a null name (should throw an exception)
    @Test(expected = IllegalArgumentException.class)
    public void testAddHeader_NameIsNull() {
    	
        email.addHeader(null, "SomeValue"); // Null header name
        
    }
    

    // Test adding a header with an empty value (should throw an exception)
    @Test(expected = IllegalArgumentException.class)
    public void testAddHeader_ValueIsEmpty() {
    	
        email.addHeader("Header", ""); // Empty header value
        
    }

    
    // Test adding a header with a null value (should throw an exception)
    @Test(expected = IllegalArgumentException.class)
    public void testAddHeader_ValueIsNull() {
    	
        email.addHeader("Header", null); // Null header value
        
    }

    
    // Test building a MIME message and ensuring BCC and CC recipients are properly set
    @Test
    public void testMimeMessageEmptySubject() throws Exception {
        email.addBcc(TEST_EMAILS);
        email.addCc(TEST_EMAILS);
        email.addHeader("Fatima Hazime", "test@abc.com");
        email.addReplyTo("test@cba.com");

        email.setHostName("localhost");
        email.setSubject("Test Subject");
        email.setBounceAddress("MimeTest@gmail.com");
        email.setMsg("TEST");

        email.buildMimeMessage(); // Construct the MIME message
        MimeMessage msg = email.getMimeMessage();

        assertEquals(3, msg.getRecipients(Message.RecipientType.BCC).length); 
        assertEquals(3, msg.getRecipients(Message.RecipientType.CC).length);
    }

    // Test POP before SMTP authentication setup in the email class
    @Test
    public void testBuildMimeMessage_PopBeforeSmtp() throws Exception {
        setField("popBeforeSmtp", true);
        setField("popHost", "pop.example.com");
        setField("popUsername", "testUser");
        setField("popPassword", "testPass");

        setField("fromAddress", new InternetAddress("test@abc.com"));
        email.addTo("test@cba.com");
        setField("content", "POP before SMTP content");

        // Provide a session that uses the "pop3" protocol
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "pop3");
        Session s = Session.getInstance(props);
        setField("session", s);

        // Attempt to build the message
        try {
            email.buildMimeMessage();
        } catch (EmailException e) {

        }
        assertNotNull(email.message);
    }
    

    // Helper method to modify private fields using reflection
    private void setField(String fieldName, Object value) throws Exception {
        Field field = Email.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(email, value);
    }

    // Test retrieving host name when no session exists, but a hostname is set
    @Test
    public void testGetHostName_NoSessionWithHostName() throws Exception {
    	
        setField("session", null);
        setField("hostName", "myserver.com");

        String hostName = email.getHostName();
        assertEquals("myserver.com", hostName);
        
    }

    
    // Test retrieving host name when no session and no hostname is set (should return null)
    @Test
    public void testGetHostName_NoSessionNoHostName() throws Exception {
    	
        setField("session", null);
        setField("hostName", null);

        String hostName = email.getHostName();
        assertNull(hostName);
        
    }
    

    // Test retrieving an existing email session
    @Test
    public void testGetMailSession_ExistingSession() throws Exception {
    	
        Session existingSession = Session.getInstance(new Properties());
        setField("session", existingSession);

        Session returnedSession = email.getMailSession();
        assertSame(existingSession, returnedSession); // Ensures the same session is returned
        
    }
    

    // Test attempting to get a mail session when no hostname is set (should throw an exception)
    @Test(expected = EmailException.class)
    public void testGetMailSession_NoHostName_ThrowsEmailException() throws Exception {
    	
        setField("session", null);
        setField("hostName", null);
        System.clearProperty("mail.host");
        email.getMailSession();
        
    }

    
    // Test setting and retrieving the sent date of an email
    @Test
    public void testGetSentDate() {
    	
        Date now = new Date();
        email.setSentDate(now);

        Date result = email.getSentDate();
        assertNotNull(result);
        assertEquals(now.getTime(), result.getTime()); // Ensure the timestamps match
        assertNotSame(now, result); // Ensure the returned object is a new instance, not the same reference
        
    }
    

    // Test getting the sent date when it hasn't been explicitly set (should return a default value)
    @Test
    public void testGetSentDateWhenNull() {
    	
        Date returnedDate = email.getSentDate();
        assertNotNull(returnedDate);
        
    }
    

 
    

    // Test setting a valid sender email address
    @Test
    public void testSetFrom_ValidEmail() throws EmailException {
    	
        String validEmail = "test@abc.com";

        Email returnedEmail = email.setFrom(validEmail);
        assertSame(email, returnedEmail);
        assertNotNull(email.getFromAddress());
        assertEquals("test@abc.com", email.getFromAddress().getAddress());
        
    }
}

var mail = actions.create("mail");   
mail.parameters.to = "your.mail@domain.com";   
mail.parameters.subject = "Test email";   
mail.parameters.text = "Outbound mail configuration is working properly";   
mail.execute(companyhome); 

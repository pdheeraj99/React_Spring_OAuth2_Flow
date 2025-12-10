1. Step 1
When hitting the localhost:8080/

![alt text](image.png)

2. When Clicked on any account
It comes to OAuth2LoginAuthenticationFilter and attempts Authentication

![alt text](image-1.png)

3. We can see here carefully now we will get the code 

![alt text](image-3.png)

4. Now using the code we will get access token and profile directly from google

![alt text](image-4.png)

![alt text](image-5.png)

5. Also see the principal which we are getting

![alt text](image-6.png)

6. So we are getting ACCESS_TOKEN
and PRINCIPAL

7. Now see how we are getting these details in our controller

OAuth2AuthorizedClient

Right now see there is nothing here
(btw we already got the principal)
![alt text](image-7.png)

Now see here we are getting AuthorizedClient

![alt text](image-8.png)


### Install pgadmin

1. Now install the PostgreSQL admin tool. All of the command will be provided below. The website from where these commands were taken is found here:  [Click here to view Pgadmin Install for Ubuntu 20.40.4](https://yallalabs.com/linux/how-to-install-pgadmin4-ubuntu-20-04/)

2. Run the command below to install the pgAdmin4 key.

`$ curl https://www.pgadmin.org/static/packages_pgadmin_org.pub | sudo apt-key add`

3. pgAdmin4 requires you to add an external repository.

`$ sudo sh -c 'echo "deb https://ftp.postgresql.org/pub/pgadmin/pgadmin4/apt/$(lsb_release -cs) pgadmin4 main" > /etc/apt/sources.list.d/pgadmin4.list && apt update'`

4. Now install pgAdmin as server mode.

`$ sudo apt install pgadmin4-web`

5. Now configure pgAdmin by running the command below.

`$ sudo /usr/pgadmin4/bin/setup-web.sh`

`The output should look like what you see below. You will be prompted for an email address and password. You will also be prompted to restart the apache server. `

`Setting up pgAdmin 4 in web mode on a Debian platform...`
`Creating configuration database...`
`NOTE: Configuring authentication for SERVER mode.`

`Enter the email address and password to use for the initial pgAdmin user account:`

`Email address: salmon@salmon.com`
`Password:`
`Retype password:`
`pgAdmin 4 - Application Initialisation`
`======================================`

`Creating storage and log directories...`
`We can now configure the Apache Web server for you. This involves enabling the wsgi module and configuring the pgAdmin 4 application to mount at /pgadmin4. Do you wish to continue (y/n)? y`
`The Apache web server is running. A restart is required for the pgAdmin 4 installation to complete. Would you like to continue (y/n)? y`
`Apache successfully restarted. You can now start using pgAdmin 4 in web mode`

6. Now you can run pgAdmin by opening a web browser and entering the URL below.

[http://localhost/pgadmin4/](http://localhost/pgadmin4/)

7. Login with the email address and password you previously created in step 9.

![change postgres password](images/pgadmin%2001-1%20login.PNG)

8. Right click on Servers -> Create -> Server

![server add](images/pgadmin%2002%20server%20add.PNG)

9. Add name to General tab.

![Add Name](images/pgadmin%2003%20Add%20Name.PNG)

10. Fill out the fields in the Connection tab.

![Add Connection](images/pgadmin%2004%20Add%20Connection.PNG)

11. Right click on the Login/Group Roles icon.

![Add User](images/pgadmin%2005%20Add%20User.PNG)

12. On the General tab type “equellauser” for the Name field.

![Add User Name gen](images/pgadmin%2005%20Add%20User%20Name%20gen.PNG)

13. On the Definition tab enter the password.

![Add password](images/pgadmin%2005%20Add%20password.PNG)

14. On the Privileges tab make sure and enable login.

![Add Role](images/pgadmin%2005%20Add%20Role.PNG)

15. Right click on the Databases icon => Create => Database.

![Add Database](images/pgadmin%2006%20Add%20Database.PNG)

16. On the General tab type “equella” for Database field and select “equellauser” from the owner dropdown. Then click save.

![Add Database name](images/pgadmin%2007%20Add%20Database%20name.PNG)

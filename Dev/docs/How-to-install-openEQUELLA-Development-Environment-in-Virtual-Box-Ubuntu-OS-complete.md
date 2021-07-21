### Contents

* [Download Ubuntu Desktop Software](#download-ubuntu)
* [Install Oracle Virtual Box VM Software](#install-oracle)
* [Install sdkman to Run and Manage Java 8](#install-sdkman)
* [Install Postgresql](#install-postgres)
* [Install Pgadmin4](#install-pgadmin)
* [Install libtinfo5](#install-libinfo5)
* [Install npm](#install-npm)
* [Install Nodejs](#install-node)
* [Install ImageMagick](#install-imagemagick)
* [Install Git](#install-git)
* [Install Intellij](#install-intellij)
* [Generate an SSH key and Add it to the ssh-agent](#install-ssh)
* [Add the ssh key to your openEQUELLA GitHub Repository](#add-key)
* [Clone the openEQUELLA Repository](#clone-repo)
* [Install SBT](#install-sbt)
* [Create a New Branch of openEQUELLA in Git](#create-branch)
* [Import sbt project into Intellij](#import-sbt)
* [Compile and Run openEQUELLA Serve](#compile-run)






***

### <a id="user-content-download-ubuntu" class="anchor" aria-hidden="true" href="#download-ubuntu"></a> Download Ubuntu Desktop Software


1. Click here to download Ubuntu

![download ubuntu](images/13%20Download%20Ubuntu.PNG)

2. Save the file to a location you will remember. You will need the install package later when you create the VM.

### <a id="user-content-install-oracle" class="anchor" aria-hidden="true" href="#install-oracle"></a> Install Oracle Virtual Box VM Software

Before installing Virtual Box on Windows 10 you must turn off the Containers and Hyper-V features.

1. To do this click on the Windows Start Menu and type “Turn Windows” which should bring up the option you see below.
2. Click on “Open.

![turn off windows features](images/01%20turn%20windows%20features%20off.jpg)

3. Now uncheck Containers and Hyper-V.

![turn off hyperv and containers](images/02%20turn%20off%20hyperV%20and%20containers.jpg)

Now you can download Oracle Virtual Box

4. [Click here to download VirtualBox](https://www.virtualbox.org/wiki/Downloads)

5. Download the version for Windows hosts

![download virtual box](images/03%20Download%20Virtual%20Box%20for%20Windows.jpg)

6. Once downloaded click on and run the installation file.

![run vb install](images/04%20Run%20VB%20install.PNG)

7. Complete the VirtualBox installation and start VirtualBox.

![finish install](images/05%20finish%20install.PNG)

8. Click the “New” button to create a new VM.

![add new vm](images/06%20Add%20New%20VM.PNG)

9. Name the VM “ubu_equella”, type should be “Linux”, and version should be “Ubuntu (64-bit)”

![name vm](images/07%20Name%20VM.PNG)

10. Set the memory size to at least 4 gigabytes. **Depending on your computer, it may not be able to handle more than this and will have to decrease the size**. Optimally 6 gigabytes would be better. If your VM fails to create, try lowering the amount of memory you allocate.

![set memory](images/08%20Set%20Memory.PNG)

11. Select “Create a virtual hard disk now”

![Create Virtual Hard Disk](images/09%20Create%20Virtual%20Hard%20Disk.PNG)

12. Select VDI (VirtualBox Disk Image)

![Create Virtual Hard Disk2](images/09.5%20Create%20Virtual%20Hard%20Disk2.PNG)

11. Select “Dynamically allocated”

![Dynamic Allocation](images/09.7%20Dynamic%20Allocation.PNG)

12. Set disk size to at least 25 gigabytes.

![Set disk size](images/10%20Set%20disk%20size.PNG)

13. The disk creation will take a few minutes to complete.

![Creating Disk](images/11%20Creating%20Disk.PNG)

14. Now that the VM has been created, click on Start -> Normal Start.

![Start vm](images/12%20Start%20vm.PNG)

15. Add a Ubuntu Image.

![Add Ubuntu Image](images/14%20Add%20Ubuntu%20Image.PNG)

15. Browse to and select the Ubuntu install you downloaded earlier.


![choose ubuntu](images/15%20choose%20ubuntu.PNG)

16. Install Ubuntu

![install ubuntu](images/16%20install%20ubuntu.PNG)

17. Select the appropriate language.

![Select English](images/17%20Select%20English.PNG)

18. Select Normal Installation

![Normal Install](images/18%20Normal%20Install.PNG)

19. Select Erase disk and install Ubuntu. Note, this will not erase the hard disk of the host computer. It will simply use allocated disk space on the host computer.

![Install Type](images/19%20Install%20Type.PNG)

20. Press Continue.

![Continue install](images/20%20Continue%20install.PNG)

21. Select the appropriate time zone.

![Time Zone](images/21%20Time%20Zone.PNG)

22. Set the name, computer name, username, and select an easy password you can remember. Require the password to login.

![ who are you](images/22%20who%20are%20you.PNG)

23. Let the installation run which will take a few minutes to complete.

![installing](images/23%20installing.PNG)

24. When prompted select Restart Now

![ Installation complete](images/24%20Installation%20complete.PNG)

25. At this screen, just click enter.

![Click enter](images/25%20Click%20enter.PNG)

26. Click on the account you created to login.

![Login](images/26%20Login.PNG)

27. Enter the password you created.

![Enter Pasword](images/27%20Enter%20Pasword.PNG)

27. The installation is now complete.

![Installation complete](images/28%20Installation%20complete.PNG)

28. Modify the display by clicking on the applications waffle menu

![Show applications](images/29%20Show%20applications.png)

29. Type displays in the applications search box.

![Type displays](images/30%20Type%20displays.png)

30. Change the display to 1280 x 800

![Change display to 1280 x 800](images/32%20Change%20displayto%201280%20x%20800.png)

### <a id="user-content-install-sdkman" class="anchor" aria-hidden="true" href="#install-sdkman"></a>Install sdkman to Run and Manage Java 8

1. From the applications waffle menu type “terminal” in the search box.

![get terminal2](images/34%20get%20terminal2.PNG)

2. Right click on the terminal icon and select “Add to Favorites”

![Add to favorites](images/35%20Add%20to%20favorites.PNG)

3. Click on the terminal icon.

![Open terminal](images/36%20Open%20terminal.PNG)

4. Now that the terminal is launched, open up a browser window and [Click here to download sdkman](https://sdkman.io/install).

5. This will show the commands to install sdkman and Java 8, but I will provide the commands below.

6. In the terminal window type the following command.

`$ curl -s "https://get.sdkman.io" | bash`

![get curl](images/37%20get%20curl.PNG)

7. You will get an error that curl is not install, but you can install it as prompted with the command. When prompted you will need to enter the administrator password you setup when you installed Ubuntu.

`$ sudo apt install curl`

![run curl install](images/38%20run%20curl%20install.PNG)

8. Curl will be installed. Now you can rerun the command below to install sdkman. When installed the output will look like the screen below.

`$ curl -s "https://get.sdkman.io" | bash`

![Install sdkman](images/39%20Install%20sdkman.PNG)

9. Now issue the command below.

`$ source "$HOME/.sdkman/bin/sdkman-init.sh"`

![configure sdkman](images/40%20configure%20sdkman.PNG)

10. Now run the command below. If it returns an sdkman version number, the installation and configuration were successful.

`$ sdk version`

11. If sdkman was correctly installed, the output will look like the screen below.

![sdkman version](images/41%20sdkman%20version.PNG)

12. Now it’s time to install Java 8 using sdkman. In order to do this you will want to list the Java versions available in sdkman by typing the command below. From the output of the screen we want the Java 8 version from java.net. You will need to scroll up a little bit to see the correct Java version highlighted below.

`$ sdk list java`

![SDK list java](images/42%20SDK%20list%20java.PNG)

13. Now that we have the correct version, we type the command as follows.

`$ sdk install java 8.0.265-open`

![Install SDK java 8](images/44%20Install%20SDK java 8.PNG)

14. Below is the output after installing the jdk.

![Install output](images/45%20Install%20output.PNG)

15. Now type the command below to verify installation

`$ java -version`

![Java version](images/46%20Java%20version.PNG)

### <a id="user-content-install-postgres" class="anchor" aria-hidden="true" href="#install-postgres"></a>Install Postgresql

1. You can install PostgreSQL in a variety of ways. I will provide a set of instructions that can be referenced here:[Click to view PostgreSQL instructions from computing for geeks.](https://computingforgeeks.com/install-postgresql-12-on-ubuntu/)

2. Run the following command below from a command prompt. You will be prompted for your administrator password.

`$ sudo apt update`</br>
`$ sudo apt -y install vim bash-completion wget`</br>
`$ sudo apt -y upgrade`</br>

3. You must reboot whenever you upgrade.

`$ sudo reboot`

4. Now add the repository.

`$ wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -`

5. Install postgresql

`$ sudo apt update`</br>
`$ sudo apt -y install postgresql-12 postgresql-client-12 postgresql-contrib-12`

6. Check to see that you can connect to postgresql using psql.

`$ sudo -u postgres psql -c "SELECT version();"`

![check version](images/pg%2001%20check%20version.png)

7. Set the password for the use postgres. To do this follow the commands below.

`$ sudo -u postgres psql postgres`</br>
`# \password postgres`

The output will look like this.

![change postgres password](images/postgresql/01%20change%20postgres%20password.png)

8. Type “exit” to escape root.

### <a id="user-content-install-pgadmin" class="anchor" aria-hidden="true" href="#install-pgadmin"></a>Install Pgadmin4

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

### <a id="user-content-install-libinfo5" class="anchor" aria-hidden="true" href="#install-libinfo5"></a>Install libtinfo5

1. Libtinfo5 is sometimes missing from the build.

`$ sudo apt install libtinfo5`

### <a id="user-content-install-npm" class="anchor" aria-hidden="true" href="#install-npm"></a>Install npm

`$ sudo apt install npm`

### <a id="user-content-install-node" class="anchor" aria-hidden="true" href="#install-node"></a>Install Nodejs

Nodejs is necessary for the project

`$ sudo apt install nodejs`

### <a id="user-content-install-imagemagick" class="anchor" aria-hidden="true" href="#install-imagemagick"></a>Install ImageMagick

1. Open a terminal window and type the following.

`$ sudo apt update`

2. Make note of the location where ImageMagick was installed.

`$ whereis convert`

![install imagemagick](images/im%2001%20magick.png)

### <a id="user-content-install-git" class="anchor" aria-hidden="true" href="#install-git"></a>Install Git

Run the command. (Some of these instructions were copied directly from GitHub Docs.)
[Click here for Github docs](https://docs.github.com/en/github/authenticating-to-github/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent)

`$ sudo apt-get install git`

### <a id="user-content-install-intellij" class="anchor" aria-hidden="true" href="#install-intellij"></a>Install Intellij

`$ sudo snap install intellij-idea-educational --classic`

### <a id="user-content-install-ssh" class="anchor" aria-hidden="true" href="#install-ssh"></a>Generate an SSH key and Add it to the ssh-agent

1. Ask for access to the BYUI openEquella repository in GitHub or fork your own branch.

2. Generate a new SSH key (this is necessary to check the code in and out from GitHub. Open a terminal and run the command below.

`$ ssh-keygen -t rsa -b 4096 -C "your_email@example.com"`

output: `Generating public/private rsa key pair.`

3. When you're prompted to "Enter a file in which to save the key," press Enter. This accepts the default file location.

`> Enter a file in which to save the key (/home/you/.ssh/id_rsa): [Press enter]`

4. At the prompt, type a secure passphrase. For more information, [see "Working with SSH key passphrases".](https://docs.github.com/en/articles/working-with-ssh-key-passphrases)

`> Enter passphrase (empty for no passphrase): [Type a passphrase]`
`> Enter same passphrase again: [Type passphrase again]`

5. Now you just add the SSH key to the ssh-agent. To do this start the ssh agent in the background.

`$ eval "$(ssh-agent -s)"`
`> Agent pid 59566`

6. Now add the SSH private key to the ssh-agent. Use the same name that you specified in step four (id_rsa)

`$ ssh-add ~/.ssh/id_rsa`</br>
`> Enter passphrase for /home/developer/.ssh/id_rsa`

### <a id="user-content-add-key" class="anchor" aria-hidden="true" href="#add-key"></a>Add the ssh key to your openEQUELLA GitHub Repository

1. Navigate to your .ssh directory and type the following:

`$ cat id_rsa.pub`

![copy key](images/github/02%20copy%20key.png)

2. Highlight the ssh key, right click and select “copy”.

![copy key2](images/github/03%20copy%20key2.png)

3. Login to your GitHub account and go to the openEQUELLA GitHub repository. Then right click on the profile icon in the upper right hand corner of the screen and select “Settings”

![settings](images/github/01%20settings.png)

4. Click on SSH and GPG keys.

![copy key2](images/github/04%20copy%20key2.png)

5. Click on the “New SSH Key” button.

![new key](images/github/06%20new%20key.png)

6. Give the ssh key a name, and past the key into the Key field. Then click the “Add SSH key” button.

![paste key](images/github/07%20paste%20key.png)

### <a id="user-content-clone-repo" class="anchor" aria-hidden="true" href="#clone-repo"></a>Clone the openEQUELLA Repository

1. Navigate to the home page of the openEQUELLA repository and click on the green “Code” button. Then copy the git command to “Clone with SSH”.

![get code](images/clone/01%20get%20code.png)

2. From the command line:
a. Navigate to your home directory.
b. Create a directory where you will store your git repositories
c. Configure your git name and email
d. Clone the openEquella repository

`$ cd /home/developer`</br>
`$ mkdir git_proj`</br>
`$ git config --global user.name “mjm”`</br>
`$ git config --global user.email someones@gmail.com`</br>
`$ git clone git@github.com:someone/openEQUELLA.git `

![clone2](images/clone/03%20clone2.png)

### <a id="user-content-install-sbt" class="anchor" aria-hidden="true" href="#install-sbt"></a>Install SBT

SBT is used to make the project. Because we installed sdkman to install Java, we can also use sdkman to install SBT, follow the commands below.

`$ sdkman install sbt`

### <a id="user-content-create-branch" class="anchor" aria-hidden="true" href="#create-branch"></a>Create a New Branch of openEQUELLA in Git

1. It’s best not to make changes directly to the develop branch. So create a new branch in Git.

Verify you are on currently on the develop branch.

`$ git branch --show-current`

![git ssh](images/clone/05%20git%20ssh.png)

2. Associate the originating openEQUELLA repo with your fork.

`$ git remote add upstream git@github.com:apereo/openEQUELLA.git`

3. Verify you are using SSH and have the upstream repository set.

`$ git remote -v`

![git ssh](images/clone/05%20git%20ssh.png)

4. Make sure your develop branch is up to date.

`$ git pull`</br>
`$ git push`

5. Create the new branch and switch to the new branch in a single command.

`git checkout -b enhancement1`

![git new branch](images/clone/06%20git%20new%20branch.png)

`git remote set-url origin'</br>
 'git@github.com:someone/openEQUELLA.git`

### <a id="user-content-import-sbt" class="anchor" aria-hidden="true" href="#import-sbt"></a>Import sbt project into Intellij

1. An easy way to make sure your dependencies are updated is to open the openEquella Respository as an sbt project. Intellij will prompt you to update your dependencies. To do this launch Intellij and click on “Open or Import.”

![import proj](images/Intellij/01%20import%20proj.png)

2. Navigate to build.sbt and click ok.

![sbt file](images/Intellij/02%20sbt%20file.png)

3. Click “Open as Project”

![open as project](images/Intellij/03%20open%20as%20project.png)

4. If you have messages in the Event Log prompting you to update dependencies, go ahead and update them.

### <a id="user-content-compile-run" class="anchor" aria-hidden="true" href="#compile-run"></a>Compile and Run openEQUELLA Server

1. f you don't have enough RAM on your host OS, please bring your max heap config down to 3072 or even lower to 2048 if sbt fails ( via the -mem in **.sbtopts** ).

NOTE:  If you cannot cannot see **.sbtopts** from the file manager you can type cntr +H which will reveal hidden files. The other options is to
 access the file from a terminal windows. Navigate to the openEquella directory and type the command below.

![sbtopts](images/Intellij/04%20sbtopts.png)

2. Either way change the value of –mem to 3072 or 2048 then cntr + o to save changes to the file. Then cntr + x to close the file.

![set mem](images/Intellij/05%20set%20mem.png)

3. cd to the {Equella repo} directory then run the command below.

`$ sbt installerZip`

4. Run the commands in the following order.

`Sbt:Equella> compile`</br>
`Sbt:Equella> prepareDevConfig`

5. (not in the sbt shell) access the Dev/learningedge-config folder using the file manager, and update **hibernate.properties** and **mandatory-config.properties** to match your environment (postgresql database and admin.url. You can edit the files by right clicking on each one and selected “Open With Text Editor.”

**hibernate.properties**
![hibernate config](images/Intellij/07%20hibernate%20config.png)

**mandatory-config.properties**
![mand config](images/Intellij/08%20mand%20config.png)

6. Go back to a terminal window and navigate to the root directory of the repository and type the command below.

`$ sbt equellaserver/run`

7. The Equella server has successfully started when the output from the terminal looks like the screen below.

![equella server start](images/Intellij/09%20equella%20server%20start.png)

8. Access the URL you set for the admin.url in mandatory-config.properties in the browser. In this case I have set the admin.url to http://localhost:8080. Complete the setup screen (arbitrary values for email / smtp servers are ok).

![install](images/postgresql/eq%2001%20install.png)

9. You may have to wait a few minutes at this screen. If the screen does not refresh after 30 minutes, you may need to manually refresh.

![install continued](images/postgresql/eq%2002%20install.png)

10. On this screen click on the “Initialize Database” button.

![initialize](images/postgresql/eq%2003%20initialize.png)

11. You may have to wait 15 minutes or more on this screen. If after 30 minutes the screen does not change, shutdown the equellaserver process from the terminal and restart.

![initialize2](images/postgresql/eq%2004%20initialize.png)

12. Download the sample institution file to your local machine. [Click here to download Sample Institution File](https://raw.githubusercontent.com/wiki/matjmiles/openEQUELLA/vanilla-scrubbed-institution.tgz)

13. Navigate to the location where you downloaded the institution file and import the file. Do not unzip or untar the file.

![add institution](images/postgresql/add%20institution.png)

14. Configure the institution. I have chosen port 8080 as to not conflict with port 80 which is the default port for http protocol. Add a password Click “Import new Institution.”

![install](images/postgresql/eq%2001%20install.png)

15. Enter the password you created previously when configuring the institution.

![authenticate](images/postgresql/eq%2010%20authenticate.png)

16. Confirm password

![confirm password](images/postgresql/eq%2010%20confirm password.png)

17. Institution will be imported. When completed scroll to the bottom of the screen and press “Return to System Management.”

![return](images/postgresql/eq%2011%20return.png)

18. Login

![login](images/postgresql/eq%2012%20login.png)

19. Use the login TLE_ADMINISTRATOR with the institution password you created previously.

![TLE](images/postgresql/eq%2013%20TLE.png)

20. You have now successfully created the openEQUELLA dev environment.

![dev complete](images/postgresql/eq%2013%20welcome.png)

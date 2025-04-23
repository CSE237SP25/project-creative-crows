# cse237-project25

Team Members:

* Amuka Shrestha
* Tariq Jassim
* Haoting Shen
* Ica Chen

# What user stories were completed last iteration?
1. Users should be saved persistently across sessions
2. Transactions should be saved persistently across sessions
3. A user should have a unique account number
4. A user should be able to reset (or recover) password
5. A user should be able to transfer money to another user
6. A banking app should validate user inputs
7. A user should be allowed to change username
8. A charge issue should increase the issuer's balance by that amount

# What user stories were completed this iteration?
1. An admin can freeze/unfreeze user accounts
2. A menu should list options in two columns
3. A User should be able to get loans
4. An admin should be able to approve loans
5. A banking App should be allowed to auto approve loans
6. A banking App should be able to configure the default admin account
7. An admin should be able to recall transactions
8. A user should be able to pay back approved loans
9. A transaction should have a unique ID, a bidirectional pair should share the same ID
10. An admin should have extra menu options in a seperate panel than standard users

# Is there anything that you implemented but doesn't currently work?

NA.

Note: To use the `Enable 2FA Recovery` option, you will need some form of 2FA installed on your phone or computer. Apps like [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2), [Authy](https://www.authy.com/) and [FreeOTP](https://freeotp.github.io/) are commonly used, but there are many more. For example, you can simply use DuoMobile to scan the QR code. Once the account is added, you will be able to use the `Reset Password` option on the login page to reset your password without logging in.

Note: We have initialized an administrator account with the username **"admin"** and an **supersecurepassword** by default. To change this, run ```
./run.sh --adminUser NAME --adminPassword PASS
```
and if you would like loans to be auto approved, add the flag `--autoApproveLoans true`
You can log in as an administrator from the login page using these credentials to test admin functionalities.
# What commands are needed to compile and run your code from the command line?
```
git clone https://github.com/CSE237SP25/project-creative-crows.git
./run.sh
```
One thing to note is that when entering account number, do not have whitespace around it.

# cse237-project25

Team Members:

* Amuka Shrestha
* Tariq Jassim
* Haoting Shen
* Ica Chen

# What user stories were completed last iteration?
1. A Menu should be able to authenticate an existing user
2. A Menu should be able to create a new user
3. A Menu should print relevant commands
4. An account should be able to deposit
5. An account should be able to withdraw
6. A Database should be able to get and set user data
7. A Database should be able to get and set transaction history
8. An account should be able to issue a charge to itself (e.g. a service fee) or to another user (e.g. a merchant charging a customer)
9. An account should be able to request their statement

# What user stories were complete this iteration?
1. Users should be saved persistently across sessions
2. Transactions should be saved persistently across sessions
3. A user should have a unique ID
4. A user should be able to reset (or recover) password
5. A user should be able to transfer money to another user
6. A banking app should validate user inputs
7. A user should be allowed to change username
8. A charge issues should increase issuer's balance by that amount

# What user stories do you intend to complete next iteration?
1. A user account can be freezed
2. A transaction can be recalled
3. A User should be able to get loans
4. A credit card should allow users to user spend more than they have, but no more than the limit
5. A user should choose to pay with credit card or balance
6. An admin should approve/reject loans
7. An admin should be able to recall transactions
8. An admin should be able to freeze a user account


# Is there anything that you implemented but doesn't currently work?

NA.

To use the `recoverAccount` option, you will need some form of 2FA installed on your phone or computer. Apps like [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2), [Authy](https://www.authy.com/) and [FreeOTP](https://freeotp.github.io/) are commonly used, but there are many, many more.

# What commands are needed to compile and run your code from the command line?
```
git clone https://github.com/CSE237SP25/project-creative-crows.git
./run.sh
```
One thing to note is that when entering account number, do not have whitespace around it.

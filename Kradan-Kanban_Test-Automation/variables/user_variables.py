class TestUser:
    def __init__(self, username, password):
        self.username = username
        self.password = password


# normal user
USER_1 = TestUser('itbkk.somchai', 'ip23/SOM')
USER_2 = TestUser('itbkk.somkiat', 'ip23/SOM')
USER_3 = TestUser('itbkk.somsuan', 'ip23/SOM')

# invalid user
USER_EMPTY = TestUser('', '')
USER_NOT_REGISTERED = TestUser('itbkk.capybara', 'ip23/CAP')
USER_EMPTY_PASSWORD = TestUser('itbkk.somchai' , '')
USER_EMPTY_USERNAME = TestUser('', 'ip23/SOM')
import sys, Ice
from bank import *
from bank_ice import *

loggedInAccount = None
loggedInContext = None
loggedInPremiumAccount = None
obj = None

def handleLoggedInStandardMenu():
    repeat = True
    while (repeat):
        print('Enter number to choose: ')
        print('1. Get account balance')
        print('2. Log out')
        print('3. Exit')
        try:
            option = int(input())
            if (option >= 1 and option <= 3):
                repeat = False
        except:
            print('\nEnter correct number\n')
            pass

    if option == 1:
        try:
            balance = loggedInAccount.getBalance(loggedInContext['pesel'], loggedInContext)
            print('\nAccount balance: {}\n'.format(balance))
            handleLoggedInStandardMenu()
        except InvalidCredentialsException as e:
            print('\nInvalid credentials - please log in again\n')
            logout()
            handleMainMenu()
    elif option == 2:
        logout()
        print('\nLogged out\n')
        handleMainMenu()
    else:
        exit(1)

def stringToCurrency(name):
    currencies = { 'USD' : Currency.USD, 'CHF' : Currency.CHF, 'GBP' : Currency.GBP, 'EUR' : Currency.EUR }
    return currencies[name]


def handleLoggedInPremiumMenu():
    repeat = True
    while (repeat):
        print('Enter number to choose: ')
        print('1. Get account balance')
        print('2. Get loan info')
        print('3. Log out')
        print('4. Exit')
        try:
            option = int(input())
            if (option >= 1 and option <= 4):
                repeat = False
        except:
            print('\nEnter correct number\n')
            pass

    if option == 1:
        try:
            balance = loggedInAccount.getBalance(loggedInContext['pesel'], loggedInContext)
            print('\nAccount balance: {}\n'.format(balance))
            handleLoggedInPremiumMenu()
        except InvalidCredentialsException as e:
            print('\nInvalid credentials - please log in again\n')
            logout()
            handleMainMenu()
    elif option == 2:
        currencies = loggedInAccount.getSupportedCurrencies(loggedInContext['pesel'], loggedInContext)
        amount = int(input('Enter loan amount: '))
        currencyStr = input('Enter loan currency{}: '.format(currencies))
        currency = stringToCurrency(currencyStr)
        years = int(input('Enter duration in years: '))
        info = loggedInAccount.getLoanInfo(loggedInContext['pesel'], LoanRequest(amount, currency, years), loggedInContext)
        print('\nCost in {}: {}, cost in PLN: {}\n'.format(currency, info.givenCurrencyCost, info.naitveCurrencyCost))
        handleLoggedInPremiumMenu()
    elif option == 3:
        logout()
        print('\nLogged out\n')
        handleMainMenu()
    else:
        exit(1)

def handleMainMenu():
    repeat = True
    while (repeat):
        print('Enter number to choose: ')
        print('1. Create account')
        print('2. Log in')
        print('3. Exit')
        try:
            option = int(input())
            if (option >= 1 and option <= 3):
                repeat = False
        except:
            print('\nEnter correct number\n')
            pass
    if option == 1:
            try:    
                firstName = input('Enter first name: ')
                lastName = input('Enter last name: ')
                pesel = input('Enter pesel: ')
                income = int(input('Enter monthly income: '))
                info = obj.create(pesel, FullName(firstName, lastName), income)
                print('\nAccount type: {}, password: {}\n'.format(info.accountType, info.password))
                premium = login(info.account, pesel, info.password)
                if not premium:
                    handleLoggedInStandardMenu()
                else:
                    handleLoggedInPremiumMenu()
            except PeselAlreadyUsedException as e:
                print('\nPesel already used\n')
                handleMainMenu()
            except ValueError as e:
                print('\nIncome must be a number\n')
                handleMainMenu()
    elif option == 2:
        try:
            pesel = input('Enter pesel: ')
            password = input('Enter password: ')
            context = {'password' : password}
            account = obj.get(pesel, context)
            premium = login(account, pesel, password)
            print('\nLogged in\n')
            if not premium:
                handleLoggedInStandardMenu()
            else:
                handleLoggedInPremiumMenu()
        except AccountNotFoundException as e:
            print('\nAccount with pesel {} not found\n'.format(pesel))
            handleMainMenu()
        except InvalidCredentialsException as e:
            print('\nInvalid pesel - password combination\n')
            handleMainMenu()
    else:
        exit(0)

def login(account, pesel, password):
    global loggedInAccount, loggedInPremiumAccount, loggedInContext
    loggedInContext = {'password' : password, 'pesel' : pesel}
    premiumAccount = PremiumAccountPrx.checkedCast(account)
    if premiumAccount is None:
        loggedInAccount = account
        loggedInPremiumAccount = False
        return False
    else:
        loggedInAccount = premiumAccount
        loggedInPremiumAccount = True
        return True

def logout():
    global loggedInAccount, loggedInPremiumAccount, loggedInContext
    loggedInAccount = None
    loggedInPremiumAccount = None
    loggedInContext = None

portNumber = int(input('Enter server port number: '))

with Ice.initialize(sys.argv) as communicator:
    configStr = 'factory/factory:tcp -h localhost -p {}:udp -h localhost -p {}'.format(portNumber, portNumber)
    base = communicator.stringToProxy(configStr)
    obj = AccountFactoryPrx.checkedCast(base)
    if not obj:
        raise RuntimeError("Invalid proxy")
        exit(1)
    handleMainMenu()
package demo.deploy;

import ch.qos.logback.classic.Level;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.web3j.crypto.Credentials;
import org.web3j.deploy.Deployable;
import org.web3j.deploy.Deployer;
import org.web3j.deploy.Predeploy;
import org.web3j.generated.contracts.HelloWorld;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class MyDeploymentLogic {
    static {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }

    private String contractAddress;

    @Predeploy(profile = "rinkeby1")
    public Deployer getRinkebyDeployer1() throws IOException {
        // Note: It's not a good idea to include your private key in source code, as you might accidentally commit
        // or publish it! It would be better if we loaded it from a configuration file or environment variable, but
        // for demo simplicity, I'm including it directly below...
        final Credentials credentials = Credentials.create("c95dc4399bee610fd91f21a23bdf5b6a7ce4cf544eaa2f4dd5426ae3dd6fa2fd");

        // The default private key above will use the address 0xFc975FBed410A88f59Ab58A23aa3Cac761aaA879
        // You can view it at https://rinkeby.epirus.io/accounts/0xfc975fbed410a88f59ab58a23aa3cac761aaa879
        System.out.println("Address: " + credentials.getAddress());

        // Note: To deploy to Rinkeby via Infura, you'll need an API key from your own account.
        // You can create one for free at infura.io and update the below URL to include the API key.
        final Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/f52e8964c7734baeafbd08a6ef7b3774"));

        final BigInteger weiBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
        final BigDecimal ethBalance = Convert.fromWei(weiBalance.toString(), Convert.Unit.ETHER);
        System.out.println("Balance: " + ethBalance + " ETH");

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "rinkeby");
    }

    @Predeploy(profile = "rinkeby2")
    public Deployer getRinkebyDeployer2() throws IOException {
        // Note: It's not a good idea to include your private key in source code, as you might accidentally commit
        // or publish it! It would be better if we loaded it from a configuration file or environment variable, but
        // for demo simplicity, I'm including it directly below...
        final Credentials credentials = Credentials.create("c95dc4399bee610fd91f21a23bdf5b6a7ce4cf544eaa2f4dd5426ae3dd6fa2fd");

        // The default private key above will use the address 0xFc975FBed410A88f59Ab58A23aa3Cac761aaA879
        // You can view it at https://rinkeby.epirus.io/accounts/0xfc975fbed410a88f59ab58a23aa3cac761aaa879
        System.out.println("Address: " + credentials.getAddress());

        // Build custom HTTP client with authentication interceptor
        String httpCredentials = okhttp3.Credentials.basic("", "4327f4ba11a94c61a97c64aaf9a315d7");

        OkHttpClient httpClient = HttpService.getOkHttpClientBuilder().addInterceptor(chain -> {
            Request request = chain.request();
            Request authenticatedRequest = request.newBuilder().header("Authorization", httpCredentials).build();
            return chain.proceed(authenticatedRequest);
        }).build();

        // Note: To deploy to Rinkeby via Infura, you'll need an API key from your own account.
        // You can create one for free at infura.io and update the below URL to include the API key.
        final Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/f52e8964c7734baeafbd08a6ef7b3774", httpClient));

        final BigInteger weiBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
        final BigDecimal ethBalance = Convert.fromWei(weiBalance.toString(), Convert.Unit.ETHER);
        System.out.println("Balance: " + ethBalance + " ETH");

        return new Deployer(web3j, new FastRawTransactionManager(web3j, credentials), new DefaultGasProvider(), "rinkeby");
    }

    @Deployable(order = 0)
    public void deployContract(Deployer deployer) throws Exception {
        // Deploying our HelloWorld contract using details provided from deployer:
        HelloWorld contract = HelloWorld.deploy(
                deployer.getWeb3j(),
                deployer.getTransactionManager(),
                deployer.getGasProvider(),
                "Hello World!"
            ).send();

        System.out.println("Deployed contract to " + contract.getContractAddress());
        contractAddress = contract.getContractAddress();
    }

    @Deployable(order = 1)
    public void configureGreeting(Deployer deployer) throws Exception {
        // Loading previously deployed contract instance:
        HelloWorld contract = HelloWorld.load(
                contractAddress,
                deployer.getWeb3j(),
                deployer.getTransactionManager(),
                deployer.getGasProvider()
        );

        // Update contract greeting:
        TransactionReceipt transactionReceipt = contract.newGreeting("Hello Ethereum!").send();

        System.out.println("Updated contract greeting in tx hash: " + transactionReceipt.getTransactionHash());
    }
}

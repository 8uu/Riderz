package riderz.team10.ecse321.com.riderzpassengers.http;

public interface HttpRequestClient {
    /**
     * This method should perform mapping between a button and its actions.
     */
    void mapButtons();

    /**
     * This method should instantiate a new synchronous HTTP client and perform requests to
     * a REST API.
     */
    void syncHttpRequest();
}

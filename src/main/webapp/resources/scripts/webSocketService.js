class WebSocketService {

    constructor() {
        this.nonRegisteredSubscriptions = [];
        this.client = Stomp.over(new SockJS('/application'));

        let connectCallback = frame => {
            this.nonRegisteredSubscriptions.forEach(item => {
                this.client.subscribe(item.location, item.subscription);
            });
        };

        this.client.connect({}, connectCallback);
    }

    registerSubscription(location, subscription) {
        if (this.client.connected) {
            this.client.subscribe(location, subscription);
        } else {
            this.nonRegisteredSubscriptions.push({location, subscription});
        }
    }

    send(location, objectToSend) {
        this.client.send(location, {}, JSON.stringify(objectToSend));
    }

    disconnect() {
        this.client.disconnect();
    }

}

export default new WebSocketService();
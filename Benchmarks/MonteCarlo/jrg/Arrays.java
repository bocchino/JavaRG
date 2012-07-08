arrayclass ResultArray {
    ToResult in AppDemo.Arrays;
}

arrayclass PriceStocks<refgroup G> {
    unique(G) PriceStock<G> in AppDemo.Arrays;
}

arrayclass PathValue {
    double in Universal.Data;
}
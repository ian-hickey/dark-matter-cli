/*
  This example component represents a basic DMScript REST resource.
  This is an example REST resource that returns a list of products.
*/
@Path("")
component name="ProductResource" hint="This is a REST controller for products" {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/products")
    public Response function getProducts() {
        // You can use the Log object to log from anywhere.
        Log.info("GET Products");

        // This does not return JSON because we return text.
        var product1 = {  name: "Shoes", description: "A new pair of shoes", price: 37.00 };
        var product2 = {  name: "Shirt", description: "A new shirt", price: 17.00 };
        var products = [product1, product2];
        return Response.ok(products).build();
    }

}
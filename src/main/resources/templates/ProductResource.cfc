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

        // Serialization is automatic based on the @Produces annotation on this method
        var products = ${packageName}.model.Product.listAll();
        return Response.ok(products).build();
    }

}
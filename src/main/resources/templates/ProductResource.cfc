/*
  This example component represents a basic DMScript REST resource.
  This is an example REST resource that returns a list of products.
*/
@Path("")
@Transactional
component name="ProductResource" hint="This is a REST controller for products" {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/products")
    public Response function getProducts() {
        // You can use the Log object to log from anywhere.
        Log.info("GET All Products sorted by name");
        // Serialization is automatic based on the @Produces annotation on this method
        var products = org.acme.model.Product.listAll(Sort.by("name"));
        return Response.ok(products).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/product/{id}")
    public Response function getOneProduct(@PathParam("id") String id ) {
      Log.info("GET a Product by id");
      var product = org.acme.model.Product.findById(id);
      if (isNull(product)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      return Response.ok(product).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/product")
    public Response function createProduct(org.acme.model.Product product) {
      Log.info("Create new product");
      product.persist();
      return Response.status(Response.Status.CREATED).entity(product).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/product/{id}")
    public Response function deleteProduct(@PathParam("id") String id ) {
      Log.info("Delete product");
      var product = org.acme.model.Product.findById(id);
      if (isNull(product)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      product.delete();
      return Response.noContent().build();
    }

}
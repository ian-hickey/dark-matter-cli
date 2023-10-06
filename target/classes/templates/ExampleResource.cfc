/*
  This example component represents a basic DMScript REST resource.
  This is an example REST resource that greets the user generically or by name.
  Each method is annotated with the REST method, the path, and what that
  method consumes or produces (json, html, text)
*/
@Path("")
component name="ExampleResource" hint="This is a REST controller" {

    public function init() { }

    property name="anonGreeting" type="string" value="anonymous user";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/greet")
    public Response function greet() {
        return Response.ok("Greetings, from Dark Matter!").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/greetbyname")
    public Response function greetByName(@QueryParam("name") String name) {
        // You can use the Log object to log from anywhere.
        Log.info("Greet: " + name);

        if (!isNull(name)) {
            var greeting = "Greetings " & name & " from Dark Matter!";
            return Response.ok(greeting).build();
        }else{
            // This does not return JSON because we return text.
            return Response.ok("Greetings " & anonGreeting & " from Dark Matter!").build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/file")
    public Response function getFile(@QueryParam("name") String fileName) {
        var file = new File("checksums.txt");
       return Response.ok(file).header("Content-Disposition", "attachment;filename=" + file).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/struct")
    public struct function getStruct() {
       return {greeting: "Hello!", id: 1, audience: "User", type: "Welcome"};
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/array")
    public Response function getArray() {
       var array = [1,2,3,4,5];
       return Response.ok(array).build();
    }
}
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
        Log.info("Greet by Name called with: " + name);

        if (!isNull(name)) {
            var greeting = "Greetings " & name & " from Dark Matter!";
            var myStruct = {greeting: greeting, id: 1, audience: "User", type: "Welcome"};
            return Response.ok(myStruct).build(); /* This returns the struct as json */
        }else{
            // This does not return JSON because we return text.
            return Response.ok("Greetings " & anonGreeting & " from Dark Matter!").build();
        }
    }
}
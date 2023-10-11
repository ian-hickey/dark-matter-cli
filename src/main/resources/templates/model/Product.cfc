@Entity
@Table(name = "products")
@NamedQuery(name = "Products.findAll", query = "SELECT p FROM Product p ORDER BY p.name", hints = @QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Cacheable
component name="Product" extends="PanacheEntity" {

    /* ID field is added automatically. */

    property name="name" type="string";
    property name="description" type="string";
    property name="price" type="numeric" value="0.00";

}
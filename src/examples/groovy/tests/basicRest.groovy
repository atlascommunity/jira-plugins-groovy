import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

return Response.ok("kek ${currentUser.name}".toString()).type(MediaType.TEXT_PLAIN).build()

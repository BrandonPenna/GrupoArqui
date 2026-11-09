# TicketYa - Features y User Stories

Roles: **Administrador**, **Organizador**, **Cliente** y **Personal de acceso**.
Entre paréntesis se indica el requerimiento de la consigna y la entrega en la que se implementa.

---

## F1. Gestión de lugares y sectores (Req. 2, Entrega 1)

**US-01. Registrar un lugar con sus sectores**
Como administrador, quiero registrar un lugar con su dirección, ubicación y sectores, para que los organizadores puedan armar eventos en él.

- Dado un lugar con nombre, dirección, latitud/longitud y al menos un sector, cuando lo registro, entonces queda guardado con un ID.
- Dado un sector numerado, cuando lo cargo, entonces debo indicar cantidad de filas y asientos por fila.
- Dado un sector no numerado (por ej. "Campo"), cuando lo cargo, entonces debo indicar su capacidad máxima.

**US-02. Consultar lugares**
Como organizador, quiero ver los lugares disponibles con sus sectores, para elegir dónde hacer mi evento.

- Cuando consulto los lugares, entonces veo cada uno con sus sectores, capacidad y si son numerados.

---

## F2. Gestión y publicación de eventos (Req. 1 y 3, Entrega 1)

**US-03. Crear un evento en borrador**
Como organizador, quiero crear un evento con sus datos y el precio de cada sector, para prepararlo antes de publicarlo.

- Dado un evento con nombre, tipo, participantes, fecha, lugar y precios por sector, cuando lo creo, entonces queda en estado BORRADOR y a mi nombre.
- Dado un lugar o sector que no existe, cuando creo el evento, entonces se rechaza.
- Dado un usuario que no es organizador, cuando intenta crear un evento, entonces recibe 403.

**US-04. Publicar un evento**
Como organizador, quiero publicar mi evento, para que los clientes lo encuentren y compren entradas.

- Dado un evento en borrador con precios definidos, cuando lo publico, entonces pasa a PUBLICADO.
- Dado un evento sin precios por sector, cuando intento publicarlo, entonces se rechaza.
- Dado un evento de otro organizador, cuando intento publicarlo, entonces se rechaza.

**US-05. Suspender y reactivar un evento**
Como administrador, quiero suspender un evento publicado, para frenar la venta ante un problema sin cancelarlo.

- Dado un evento PUBLICADO o AGOTADO, cuando lo suspendo, entonces pasa a SUSPENDIDO, no aparece en la búsqueda y no acepta reservas.
- Dado un evento SUSPENDIDO, cuando lo reactivo, entonces vuelve a PUBLICADO.
- Dado un usuario que no es administrador, cuando intenta suspender, entonces recibe 403.

**US-06. Evento agotado y finalizado**
Como cliente, quiero saber si un evento está agotado o ya pasó, para no intentar comprar entradas que no existen.

- Dado un evento sin lugares disponibles, entonces se muestra como AGOTADO.
- Dado un evento cuya fecha ya pasó, entonces se muestra como FINALIZADO y no permite compras ni devoluciones.

---

## F3. Búsqueda de eventos (Req. 9, Entrega 1)

**US-07. Buscar eventos por varios criterios**
Como cliente, quiero filtrar eventos por tipo, organizador, participante, ciudad y rango de fechas, para encontrar lo que me interesa.

- Cuando combino varios filtros, entonces solo veo los eventos PUBLICADOS que cumplen todos.
- Cuando no indico filtros, entonces veo todos los eventos publicados, paginados.

**US-08. Ordenar los resultados**
Como cliente, quiero ordenar los resultados por fecha, precio o relevancia, para comparar más fácil.

- Cuando ordeno por fecha, entonces veo primero los más próximos.
- Cuando ordeno por precio, entonces se usa el precio del sector más barato de cada evento.
- Cuando busco un texto y ordeno por relevancia, entonces aparecen primero los que lo tienen en el nombre, luego en un participante y luego en la descripción.

---

## F4. Compra de entradas (Req. 4, Entrega 1)

**US-09. Reservar lugares**
Como cliente, quiero elegir sector y asientos y que me los reserven, para tener tiempo de pagar.

- Dado un sector numerado, cuando elijo asientos libres, entonces quedan reservados por 10 minutos.
- Dado un sector no numerado, cuando pido una cantidad dentro de la capacidad disponible, entonces se reserva esa cantidad.
- Dado que supero el máximo de entradas por cliente para el evento, cuando reservo, entonces se rechaza.
- Dado un evento que no está publicado, cuando reservo, entonces se rechaza con 409.

**US-10. Pagar y recibir las entradas**
Como cliente, quiero pagar mi reserva, para recibir mis entradas con un código QR.

- Dada una reserva vigente, cuando el pago es aprobado, entonces se registra la compra con el medio de pago y cada entrada recibe un QR único.
- Dado un pago rechazado, entonces la reserva se cancela y los lugares se liberan.
- Dada una reserva vencida, cuando intento pagar, entonces se rechaza y los lugares ya están liberados.

**US-11. Ver mis compras**
Como cliente, quiero consultar mis compras y entradas, para tener a mano los QR.

- Cuando consulto mis compras, entonces veo el detalle, el medio de pago y el estado de cada entrada.

---

## F5. No doble venta (Req. 5, Entrega 1)

**US-12. Un asiento se vende una sola vez**
Como cliente, quiero que el asiento que compro sea solo mío, para no tener problemas al entrar.

- Dados muchos clientes que reservan el mismo asiento al mismo tiempo, entonces solo uno lo consigue y el resto recibe 409.
- Dados muchos clientes que piden el último lugar de una zona al mismo tiempo, entonces nunca se supera la capacidad.
- Dadas varias instancias del servicio de ventas, entonces se mantiene la misma garantía.

**US-13. Reintentar el pago sin cobrar dos veces**
Como cliente, quiero poder reintentar el pago si se corta la conexión, sin que me cobren dos veces.

- Dada una reserva ya pagada, cuando reintento el pago, entonces recibo la misma compra y no se hace un segundo cobro.

---

## F6. Devoluciones y cancelaciones (Req. 7, Entrega 1)

**US-14. Solicitar la devolución de una entrada**
Como cliente, quiero pedir la devolución de una entrada, para recuperar mi dinero si no puedo ir.

- Dada una entrada vendida y faltando más de 72 horas para el evento, cuando la solicito, entonces queda una solicitud pendiente.
- Dado que faltan menos de 72 horas, cuando la solicito, entonces se rechaza.

**US-15. Aprobar o rechazar devoluciones**
Como administrador, quiero aprobar o rechazar las solicitudes, para controlar los reintegros.

- Cuando apruebo, entonces el lugar se libera y se pide el reintegro a la pasarela de pagos.
- Cuando rechazo, entonces la entrada sigue siendo válida.

**US-16. Cancelar un evento**
Como administrador u organizador del evento, quiero cancelarlo, para que todos los compradores reciban su dinero.

- Cuando cancelo un evento, entonces todas sus entradas vendidas pasan a reintegro y se notifica a los compradores.
- Dadas reservas pendientes del evento, entonces se cancelan.
- Dado que el servicio de ventas no responde, entonces el evento no se cancela y se informa el error.
- Dado un evento ya cancelado o finalizado, cuando intento cancelarlo, entonces recibo 409.

---

## F7. Fila virtual (Req. 6, Entrega 2)

**US-17. Entrar a la fila virtual**
Como cliente, quiero entrar a una fila virtual en eventos de alta demanda, para comprar en orden de llegada.

- Dado un evento de alta demanda, cuando se abre la venta, entonces quedo en la fila y veo mi posición.
- Cuando llega mi tanda, entonces se me habilita a reservar por un tiempo limitado.
- Dado que no estoy habilitado, cuando intento reservar, entonces se rechaza.

---

## F8. Control de acceso (Req. 8, Entrega 2)

**US-18. Validar entradas en el ingreso**
Como personal de acceso, quiero escanear el QR de cada entrada, para dejar pasar solo entradas válidas.

- Dado un QR válido de ese evento y sin usar, cuando lo escaneo, entonces se acepta y la entrada queda UTILIZADA.
- Dado un QR ya usado, de otro evento o inexistente, cuando lo escaneo, entonces se rechaza indicando el motivo.

---

## F9. Preferencias y recomendaciones (Req. 10, Entrega 2)

**US-19. Registrar mis preferencias**
Como cliente, quiero guardar mis tipos de evento, artistas favoritos y ciudad, para recibir sugerencias.

- Cuando guardo mis preferencias, entonces quedan asociadas a mi usuario y las puedo modificar.

**US-20. Recibir recomendaciones**
Como cliente, quiero ver eventos sugeridos, para descubrir otros que me pueden gustar.

- Cuando pido sugerencias, entonces veo eventos publicados según mis preferencias y lo que compraron otros clientes de mis mismos eventos.

---

## F10. Notificaciones (Req. 11, Entrega 2)

**US-21. Recibir avisos**
Como cliente, quiero recibir un email o push, para enterarme de lo importante sin entrar a la app.

- Cuando confirmo una compra, entonces recibo una notificación con mis entradas.
- Cuando un evento al que voy se cancela o cambia, entonces recibo un aviso.
- Cuando un artista favorito publica un evento en mi ciudad, entonces recibo un aviso.
- Dado que el servicio de notificaciones falla, entonces la compra o cancelación no se ve afectada.

---

## F11. Reportes para organizadores (Req. 12, Entrega 2)

**US-22. Ver reportes de mis eventos**
Como organizador, quiero ver entradas vendidas y devueltas por sector, ocupación y recaudación, para seguir mis ventas.

- Cuando consulto el reporte de un evento propio, entonces veo esos datos por sector.
- Dado un evento de otro organizador, cuando pido el reporte, entonces recibo 403.

---

## F12. Seguridad (Entrega 2)

**US-23. Iniciar sesión según mi rol**
Como usuario, quiero autenticarme, para usar solo las funciones de mi rol.

- Cuando inicio sesión con credenciales válidas, entonces recibo un token JWT con mi rol.
- Dado un token inválido o vencido, cuando llamo a la API, entonces recibo 401.
- Dado un rol sin permiso para una acción, cuando la intento, entonces recibo 403.

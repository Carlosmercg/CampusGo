# 📖 CampusGo Wiki

Bienvenido a la **Wiki** de *CampusGo*, la aplicación diseñada para conectar estudiantes universitarios en la compra y venta de materiales académicos dentro de los campus.

---

## 📌 Índice

1. [Inicio](#inicio)
2. [Casos de Uso](#casos-de-uso)
3. [Estructura de la Aplicación](#estructura-de-la-aplicacion)
4. [Flujo de Navegación](#flujo-de-navegacion)
5. [Base de Datos](#base-de-datos)
6. [Guía de Desarrollo](#guia-de-desarrollo)
7. [API y Funcionalidades](#api-y-funcionalidades)

---

## 🔹 Inicio

**CampusGo** es una aplicación móvil que facilita la compra y venta de materiales de estudio dentro de los campus universitarios. La plataforma fomenta la economía colaborativa y permite a los estudiantes acceder a recursos académicos de manera eficiente y accesible.

### 🌟 Objetivos
- Facilitar la conexión entre estudiantes para compra y venta de productos académicos.
- Ofrecer un sistema seguro de verificación universitaria.
- Permitir la comunicación en tiempo real entre usuarios.
- Integrar herramientas de seguimiento de entregas.

---

## 🌐 Características Principales
 
 ✨ Creación de Perfiles: Los usuarios pueden crear un perfil con su información universitaria y calificar a otros.
 
 🏰 Verificación de Usuario: Confirmación de identidad mediante reconocimiento facial, huella o escaneo de carné universitario.
 
 👉 Publicación de Productos: Posibilidad de subir fotos y descripciones de materiales a vender.
 
 🌐 Chat en Tiempo Real: Mensajería y chat de voz entre compradores y vendedores.
 
 🛠️ Seguimiento en Tiempo Real: Uso de GPS para rastrear ubicaciones de compradores y vendedores.
 
 ✨ Modo Horizontal y Vertical: Adaptabilidad de la interfaz según la orientación del dispositivo.
 

## 🔹 Casos de Uso

La aplicación maneja dos tipos de usuario: **compradores** y **vendedores**. Un mismo usuario puede desempeñar ambos roles simultáneamente.

### 🛍️ Compradores
- Explorar categorías de productos.
- Ver detalles de productos.
- Agregar productos al carrito y realizar compras.
- Comunicarse con vendedores mediante chat.
- Realizar pagos y seguir pedidos en tiempo real.

### 🏷️ Vendedores
- Publicar productos con fotos y descripciones.
- Gestionar pedidos recibidos.
- Chatear con compradores interesados.
- Actualizar estado de productos vendidos.
- Configurar métodos de pago y entrega.

---

## 🔹 Estructura de la Aplicación

La aplicación está compuesta por las siguientes actividades y vistas:

1. **MainActivity (SplashScreen)** → Pantalla de bienvenida con el logo de *CampusGo*.
2. **Login (Inicio de Sesión)/ Registro** → Pantalla para iniciar sesión o registrarse, se ingresa la información (*nombre de usuario y contraseña*) del usuario ya existente o nuevo.
3. **HomeActivity** → Menú principal que muestra las diferentes categorías (*o carreras*) en donde se pueden comprar los materiales deseados; a la vez, se puede acceder al perfil y al carrito de compras.
4. **Categorías** → Lista de categorías de productos.
5. **Subir Producto** → En donde se inserta la información (*Imágen, título, descripción, precio sugerido, y a la categoría que pertenece*) del producto que se desea vender.
6. **Lista de Productos** → Vista de productos dentro de una categoría.
7. **Detalle de Producto** → Información detallada del producto (*Título y descripción del producto*).
8. **Mensajería** → Lista de chats organizados en pestañas (*Clientes* y *Vendedores*).
9. **Chat** → Conversación con un usuario específico.
10. **Carrito de Compras** → Productos agregados con opción de pago.
12. **Seguimiento** → Ubicación en tiempo real del producto comprado y del usuario que lleva el producto hasta dicha ubicacion.
13. **Perfil** → Donde se observa la información del usuario, se puede acceder a los pedidos pasados, métodos de pago y subir un producto.
14. **Editar Perfil** → Cambiar información dentro del perfil del usuario como nombres y apellidos, ubicación (*ciudad, universidad, locación de entregas*), correo estudiantil, número de teléfono y su foto de perfil.
15. **Perfil Métodos de Pago** → Muestra la información de los métodos de pago registrados por el usuario; se puede añadir uno nuevo.
16. **Aniadir Tarjeta** → Se inserta la información requerida (*número de tarjeta, nombre y apellido del propietario, fecha de expedición y código de seguridad*) de la tarjeta de crédito o débito del usuario.
17. **Productos Vendidos** → Muestra los productos vendidos por el usuario hasta la fecha actual.
18. **Lista Compras Pasadas** → Muestra las compras realizadas por el usuario hasta la fecha actual.
19. **Mapa Comprador** → Se observa, en tiempo real, la ubicación acordada en dónde se recibirá el producto, y la ubicación actual del vendedor.
20. **Mapa Vendedor** → Se observa, en tiempo real, la ubicación acordada por el usuario para entregar el producto, y la ubicación actual del vendedor.

---

## 🔹 Flujo de Navegación

### 🔄 Proceso de Compra
1. El usuario inicia sesión y accede a la categoría deseada.
2. Dentro de la categoría, se observan varios productos.
3. Elige un producto y lo agrega al carrito.
4. Procede al pago, escogiendo el método y la ubicación deseada, y confirma la compra.
5. Contacta al vendedor si es necesario y sigue el pedido.
6. El usuario y el vendedor se encuentran la ubicación deseada.
7. Se hace contacto NFC entre el móvil del usuario y el vendedor.
8. Se recibe el pedido y se califica al vendedor.

### 📦 Proceso de Venta
1. El usuario inicia sesión y accede a su perfil.
2. Le da "click" a la opción de *Subir Producto*.
3. Llena la información necesaria del producto.
4. Publica uel producto.
5. Recibe mensajes de compradores interesados.
6. Gestiona el estado de los pedidos y confirma entregas.
7. El vendedor se encuentra con el usuario en la ubicación deseada.
8. Se hace contact NFC entre el móvil del vendedor y el usuario.
9. Se entrega el pedido.

## 👤 Proceso de Creación de Usuario (Sign-In)
1. Se crea un nombre de usuario.
2. Se crea una contraseña.
3. Se confirma dicha contraseña.
4. Se escanea el carnet del usuario para confirmar su identidad.
5. Se inserta la información necesaria dentro de su nuevo perfil (*foto de perfil, nombres y apellidos, etc*).
6. Se accede a la aplicación con éxito.

## 🔓 Proceso de Edición de Perfil
1. Se accede al perfil del usuario.
2. Se hace "click" a *Editar Perfil*.
3. Se realizan los cambios deseados.
4. Se guardan los cambios.
5. Se actualiza el perfil.

## 🪪 Proceso de Adición de Método de Pago
1. Se accede al perfil del usuario.
2. Se hace "click" en *Formas de Pago*.
3. Se hace "click" en el símbolo "+" en la parte superior derecha de la pantalla.
4. Se selecciona *Tarjeta Crédito o Débito (CCV)*.
5. Se inserta la información requerida de la tarjeta.
6. Se guarda y actualiza el menú de métodos de pago con esta nueva tarjeta.

---

## 🔹 Base de Datos

La base de datos está estructurada en las siguientes entidades:

- **Usuarios** (ID, Nombre, Universidad, Rol, Foto de perfil)
- **Productos** (ID, Vendedor, Categoría, Precio, Descripción, Estado)
- **Chats** (ID, Usuario1, Usuario2, Mensajes)
- **Pedidos** (ID, Comprador, Vendedor, Productos, Estado, Pago)
- **Métodos de Pago** (ID, Usuario, Tipo, Información de Tarjeta o Cuenta)
- **Categoría** (ID, nombre, ícono)
- **Mensaje** (ID, remitente, contenido, timestamp)


## 👥 Equipo de Desarrollo
 
 Carlos Mercado
 
 Sara Albarracín
 
 Victoria Roa
 
 Alejandro Caicedo


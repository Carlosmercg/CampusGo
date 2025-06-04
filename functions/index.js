/* eslint-disable indent, object-curly-spacing, comma-dangle, key-spacing, max-len */
const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.enviarNotificacionChat = functions.database
  .ref("/chats/{chatId}/messages/{messageId}")
  .onCreate(async (snapshot, context) => {
    const nuevoMensaje = snapshot.val();
    if (!nuevoMensaje) {
      return null;
    }

    const chatId = context.params.chatId;
    const contenido = nuevoMensaje.contenido || "";
    const uidEmisor = nuevoMensaje.emisor || "";
    const uidReceptor = nuevoMensaje.receptor || "";

    // 1) Obtener nombre del emisor desde Firestore
    let nombreEmisor = "Alguien";
    try {
      const docEmisor = await admin
        .firestore()
        .collection("usuarios")
        .doc(uidEmisor)
        .get();
      if (docEmisor.exists && docEmisor.data().nombre) {
        nombreEmisor = docEmisor.data().nombre;
      }
    } catch (error) {
      console.error("enviarNotificacionChat → Error obteniendo nombreEmisor:", error);
    }

    // 2) Obtener token FCM del receptor desde Firestore
    let tokenReceptor = null;
    try {
      const docReceptor = await admin
        .firestore()
        .collection("usuarios")
        .doc(uidReceptor)
        .get();
      if (docReceptor.exists && docReceptor.data().fcmToken) {
        tokenReceptor = docReceptor.data().fcmToken;
      }
    } catch (error) {
      console.error("enviarNotificacionChat → Error obteniendo fcmToken:", error);
    }

    if (!tokenReceptor) {
      console.log("enviarNotificacionChat → No hay token FCM para UID:", uidReceptor);
      return null;
    }

    // 3) Construir payload del Data Message para FCM
    const mensajeFCM = {
      token: tokenReceptor,
      data: {
        titulo: `${nombreEmisor} te ha enviado un mensaje`,
        cuerpo: contenido,
        nombreEmisor: nombreEmisor,
        ultimoMensaje: contenido,
        chatId: chatId,
        uidEmisor: uidEmisor,
      },
      android: {
        priority: "high",
      },
      apns: {
        headers: {
          "apns-priority": "10",
        },
        payload: {
          aps: {
            sound: "default",
          },
        },
      },
    };

    // 4) Enviar la notificación vía Admin SDK
    try {
      const response = await admin.messaging().send(mensajeFCM);
      console.log("enviarNotificacionChat → Notificación enviada:", response);
    } catch (error) {
      console.error("enviarNotificacionChat → Error enviando notificación:", error);
    }

    return null;
  });
// =================================================================================
// 2) NUEVO: Notificar al VENDEDOR que alguien lanzó una compra.
//    Escucha: /notificacionesCompra/{uidVendedor}/{notiId}
//    Envía Data Message al vendedor con tipo "nuevaCompra".
// =================================================================================
exports.notificarAlVendedor = functions.database
  .ref("/notificacionesCompra/{uidVendedor}/{notiId}")
  .onCreate(async (snapshot, context) => {
    const data = snapshot.val();
    if (!data) return null;

    const uidVendedor = context.params.uidVendedor;
    const uidComprador = data.uidComprador || "";
    const productoId = data.productoId || "";
    const chatId = data.chatId || "";
    const titulo = data.titulo || "¡Tienes una nueva solicitud de compra!";
    const cuerpo = data.body || "Toca para revisar la venta";

    // 1) Obtener fcmToken del vendedor
    let tokenVendedor = null;
    try {
      const docV = await admin.firestore().collection("usuarios").doc(uidVendedor).get();
      if (docV.exists && docV.data().fcmToken) {
        tokenVendedor = docV.data().fcmToken;
      }
    } catch (e) {
      console.error("Error al leer tokenVendedor:", e);
    }
    if (!tokenVendedor) return null;

    // 2) Construir Data Message tipo "nuevaCompra"
    const mensajeFCM = {
      token: tokenVendedor,
      data: {
        tipo: "nuevaCompra",
        chatId: chatId,
        uidComprador: uidComprador,
        productoId: productoId,
        titulo: titulo,
        body: cuerpo
      },
      android: { priority: "high" },
      apns: {
        headers: { "apns-priority": "10" },
        payload: { aps: { sound: "default" } }
      }
    };

    // 3) Enviar notificación al vendedor
    try {
      const response = await admin.messaging().send(mensajeFCM);
      console.log("Notificación de compra al vendedor enviada:", response);
    } catch (err) {
      console.error("Error enviando compra al vendedor:", err);
    }
    return null;
  });

// =================================================================================
// 3) NUEVO: Notificar al COMPRADOR si su compra fue aceptada o rechazada.
//    Escucha: /notificacionesCompra/{uidComprador}/{notiId}
//    Envía Data Message al comprador con tipo "compraAceptada" o "compraRechazada".
// =================================================================================
exports.notificarAlComprador = functions.database
  .ref("/notificacionesCompra/{uidComprador}/{notiId}")
  .onCreate(async (snapshot, context) => {
    const data = snapshot.val();
    if (!data) return null;

    const uidComprador = context.params.uidComprador;
    const uidVendedor = data.uidVendedor || "";
    const productoId = data.productoId || "";
    const chatId = data.chatId || "";
    const resultado = data.resultado || ""; // "aceptada" o "rechazada"
    const titulo = data.titulo || (
                              resultado === "aceptada" ?
                                "¡Tu compra fue aceptada!" :
                                "Tu compra fue rechazada"
                            );
    const cuerpo = data.body || "";

    // 1) Obtener fcmToken del comprador
    let tokenComprador = null;
    try {
      const docC = await admin.firestore().collection("usuarios").doc(uidComprador).get();
      if (docC.exists && docC.data().fcmToken) {
        tokenComprador = docC.data().fcmToken;
      }
    } catch (e) {
      console.error("Error al leer tokenComprador:", e);
    }
    if (!tokenComprador) return null;

    // 2) Construir Data Message tipo "compraAceptada" o "compraRechazada"
    const mensajeFCM = {
      token: tokenComprador,
      data: {
        tipo: resultado === "aceptada" ? "compraAceptada" : "compraRechazada",
        chatId: chatId,
        uidVendedor: uidVendedor,
        productoId: productoId,
        titulo: titulo,
        body: cuerpo
      },
      android: { priority: "high" },
      apns: {
        headers: { "apns-priority": "10" },
        payload: { aps: { sound: "default" } }
      }
    };

    // 3) Enviar notificación al comprador
    try {
      const response = await admin.messaging().send(mensajeFCM);
      console.log("Notificación de resultado compra al comprador enviada:", response);
    } catch (err) {
      console.error("Error enviando resultado compra al comprador:", err);
    }
    return null;
  });

package org.game.eternity2;

import org.game.eternity2.server.EternityPacket;
import org.game.eternity2.server.EternityUser;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class EternityTest {

    @Test
    public void testPacketSerialization() throws IOException, ClassNotFoundException {
        EternityUser user = new EternityUser("testUser", "password");
        EternityPacket packet = new EternityPacket(user, EternityPacket.Command.LOGIN, "Payload");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(packet);
        out.flush();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        EternityPacket deserializedPacket = (EternityPacket) in.readObject();

        assertEquals(packet.getCommand(), deserializedPacket.getCommand());
        assertNotNull(packet.getUser());
        assertEquals("testUser", packet.getUser().getLogin());
        assertEquals(packet.getPayload(), deserializedPacket.getPayload());
    }

    @Test
    public void testPacketCreation() {
        EternityUser user = new EternityUser("testUser", "password");
        EternityPacket packet = new EternityPacket(user, EternityPacket.Command.JOB_REQUEST, null);

        assertNotNull(packet);
        assertEquals(EternityPacket.Command.JOB_REQUEST, packet.getCommand());
        assertNull(packet.getPayload());
    }

    @Test
    public void testInvalidUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EternityPacket(null, EternityPacket.Command.LOGIN, null);
        });
    }
}

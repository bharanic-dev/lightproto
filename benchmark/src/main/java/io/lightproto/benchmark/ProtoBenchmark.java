package io.lightproto.benchmark;

import com.example.tutorial.AddressBook;
import com.example.tutorial.AddressBookProtos;
import com.example.tutorial.Person;
import com.google.protobuf.CodedOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 3)
@Fork(value = 1)
public class ProtoBenchmark {

    final static byte[] serialized;

    static {
        AddressBook ab = new AddressBook();
        Person p1 = ab.addPerson();
        p1.setName("name");
        p1.setEmail("name@example.com");
        p1.setId(5);
        Person.PhoneNumber p1_pn1 = p1.addPhone();
        p1_pn1.setNumber("xxx-zzz-yyyyy");
        p1_pn1.setType(Person.PhoneType.HOME);

        Person.PhoneNumber p1_pn2 = p1.addPhone();
        p1_pn2.setNumber("xxx-zzz-yyyyy");
        p1_pn2.setType(Person.PhoneType.MOBILE);

        Person p2 = ab.addPerson();
        p2.setName("name 2");
        p2.setEmail("name2@example.com");
        p2.setId(6);

        Person.PhoneNumber p2_pn1 = p2.addPhone();
        p2_pn1.setNumber("xxx-zzz-yyyyy");
        p2_pn1.setType(Person.PhoneType.HOME);

        serialized = new byte[ab.getSerializedSize()];
        ab.writeTo(Unpooled.wrappedBuffer(serialized).resetWriterIndex());
    }

    private final AddressBook frame = new AddressBook();

    private final ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(1024);
    byte[] data = new byte[1024];
    private ByteBuf serializeByteBuf = Unpooled.wrappedBuffer(serialized);

    @Benchmark
    public void protobufSerialize(Blackhole bh) throws Exception {
        AddressBookProtos.AddressBook.Builder pbab = AddressBookProtos.AddressBook.newBuilder();
        AddressBookProtos.Person.Builder pb_p1 = AddressBookProtos.Person.newBuilder();
        pb_p1.setName("name 1");
        pb_p1.setEmail("name1@example.com");
        pb_p1.setId(5);
        AddressBookProtos.Person.PhoneNumber.Builder pb1_pn1 = AddressBookProtos.Person.PhoneNumber.newBuilder();
        pb1_pn1.setNumber("xxx-zzz-1111");
        pb1_pn1.setType(AddressBookProtos.Person.PhoneType.HOME);

        AddressBookProtos.Person.PhoneNumber.Builder pb1_pn2 = AddressBookProtos.Person.PhoneNumber.newBuilder();
        pb1_pn2.setNumber("xxx-zzz-2222");
        pb1_pn2.setType(AddressBookProtos.Person.PhoneType.MOBILE);

        pb_p1.addPhone(pb1_pn1);
        pb_p1.addPhone(pb1_pn2);

        AddressBookProtos.Person.Builder pb_p2 = AddressBookProtos.Person.newBuilder();
        pb_p2.setName("name 2");
        pb_p2.setEmail("name2@example.com");
        pb_p2.setId(6);

        AddressBookProtos.Person.PhoneNumber.Builder pb2_pn1 = AddressBookProtos.Person.PhoneNumber.newBuilder();
        pb2_pn1.setNumber("xxx-zzz-2222");
        pb2_pn1.setType(AddressBookProtos.Person.PhoneType.HOME);

        pb_p2.addPhone(pb2_pn1);

        pbab.addPerson(pb_p1);
        pbab.addPerson(pb_p2);

        CodedOutputStream s = CodedOutputStream.newInstance(data);
        pbab.build().writeTo(s);

        bh.consume(pbab);
        bh.consume(s);
    }

    @Benchmark
    public void lightProtoSerialize(Blackhole bh) throws Exception {
        frame.clear();

        Person p1 = frame.addPerson();
        p1.setName("name");
        p1.setEmail("name@example.com");
        p1.setId(5);
        Person.PhoneNumber p1_pn1 = p1.addPhone();
        p1_pn1.setNumber("xxx-zzz-yyyyy");
        p1_pn1.setType(Person.PhoneType.HOME);

        Person.PhoneNumber p1_pn2 = p1.addPhone();
        p1_pn2.setNumber("xxx-zzz-yyyyy");
        p1_pn2.setType(Person.PhoneType.MOBILE);

        Person p2 = frame.addPerson();
        p2.setName("name 2");
        p2.setEmail("name2@example.com");
        p2.setId(6);

        Person.PhoneNumber p2_pn1 = p1.addPhone();
        p2_pn1.setNumber("xxx-zzz-yyyyy");
        p2_pn1.setType(Person.PhoneType.HOME);

        frame.writeTo(buffer);
        buffer.clear();

        bh.consume(frame);
    }

    @Benchmark
    public void protobufDeserialize(Blackhole bh) throws Exception {
        AddressBookProtos.AddressBook ab = AddressBookProtos.AddressBook.newBuilder().mergeFrom(serialized).build();
        bh.consume(ab);
    }

    @Benchmark
    public void lightProtoDeserialize(Blackhole bh) throws Exception {
        frame.parseFrom(serializeByteBuf, serializeByteBuf.readableBytes());
        serializeByteBuf.resetReaderIndex();
        bh.consume(frame);
    }


}

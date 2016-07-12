package com.xoa.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TTransport;

import java.nio.ByteBuffer;

/**
 * 用来对 Processor 的输入协议进行嵌入操作<br>
 * 1. 将协议字串进行可读化操作
 * 2. TODO: 添加协议数据的 DUMP 能力
 */
public class XReadableProtocol extends TProtocol {
    TProtocol source = null;
    StringBuffer sb  = null;
    String funcName;

    public static XReadableProtocol getProtocol(TProtocol in) {
        XReadableProtocol protocol = new XReadableProtocol(in.getTransport());
        protocol.setInnerProtocol(in);
        return protocol;
    }

    /**
     * 主要用于兼容父类
     */
    protected XReadableProtocol(TTransport trans) {
        super(trans);
    }


    protected void setInnerProtocol(TProtocol in) {
        source = in;
        this.sb = new StringBuffer();
    }

    public String getFuncName() {
        return this.funcName;
    }

    public String getReadableText() {
        return sb.toString();
    }

    @Override
    public TMessage readMessageBegin() throws TException {
        TMessage value = source.readMessageBegin();
        sb.append("Method:").append(value.name).append("(");
        this.funcName = value.name;
        return value;
    }

    @Override
    public void readMessageEnd() throws TException {
        sb.append(")");
        source.readMessageEnd();
    }

    @Override
    public TStruct readStructBegin() throws TException {
        TStruct value = source.readStructBegin();
        sb.append("Struct:").append(value.name).append("(");
        return value;
    }

    @Override
    public void readStructEnd() throws TException {
        source.readStructEnd();
        sb.append(")");
    }

    @Override
    public TField readFieldBegin() throws TException {
        TField value = source.readFieldBegin();
        sb.append("Field").append(value.name).append("(");
        return value;
    }

    @Override
    public void readFieldEnd() throws TException {
        source.readFieldEnd();
        sb.append(")");
    }

    @Override
    public TMap readMapBegin() throws TException {
        TMap value = source.readMapBegin();
        sb.append("Map<").append(value.keyType).append(", ").append(value.valueType)
                .append(">").append("(");
        return value;
    }

    @Override
    public void readMapEnd() throws TException {
        source.readMapEnd();
        sb.append(")");
    }

    @Override
    public TList readListBegin() throws TException {
        TList value = source.readListBegin();
        sb.append("List").append("(");
        return value;
    }

    @Override
    public void readListEnd() throws TException {
        source.readListEnd();
        sb.append(")");
    }

    @Override
    public TSet readSetBegin() throws TException {
        TSet value = source.readSetBegin();
        sb.append("Set").append("(");
        return value;
    }

    @Override
    public void readSetEnd() throws TException {
        source.readSetEnd();
        sb.append(")");
    }

    @Override
    public boolean readBool() throws TException {
        boolean value = source.readBool();
        sb.append("bool:").append(value);
        return value;
    }

    @Override
    public byte readByte() throws TException {
        byte value = source.readByte();
        sb.append("byte:").append(value);
        return value;
    }

    @Override
    public short readI16() throws TException {
        short value = source.readI16();
        sb.append("i16:").append(value);
        return value;
    }

    @Override
    public int readI32() throws TException {
        int value = source.readI32();
        sb.append("i32:").append(value);
        return value;
    }

    @Override
    public long readI64() throws TException {
        long value = source.readI64();
        sb.append("i64").append(value);
        return value;
    }

    @Override
    public double readDouble() throws TException {
        double value = source.readDouble();
        sb.append("double:").append(value);
        return value;
    }

    @Override
    public String readString() throws TException {
        String value = source.readString();
        sb.append("str:").append(value);
        return value;
    }

    @Override
    public ByteBuffer readBinary() throws TException {
        ByteBuffer value = source.readBinary();
        sb.append("binary:").append(value.array().length).append(" length");
        return value;
    }

    @Override
    public void writeMessageBegin(TMessage message) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeMessageEnd() throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeStructBegin(TStruct struct) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeStructEnd() throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeFieldBegin(TField field) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeFieldEnd() throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeFieldStop() throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeMapBegin(TMap map) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeMapEnd() throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeListBegin(TList list) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeListEnd() throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeSetBegin(TSet set) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeSetEnd() throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeBool(boolean b) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeByte(byte b) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeI16(short i16) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeI32(int i32) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeI64(long i64) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeDouble(double dub) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeString(String str) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }

    @Override
    public void writeBinary(ByteBuffer buf) throws TException {
        throw new TException(this.getClass().getName() + "Not support write method");
    }
}



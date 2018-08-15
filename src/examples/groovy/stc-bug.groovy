String content = 'asd'

OutputStream outputStream = new ByteArrayOutputStream()

outputStream.withWriter{
    it.write(content)
    it.flush()
}

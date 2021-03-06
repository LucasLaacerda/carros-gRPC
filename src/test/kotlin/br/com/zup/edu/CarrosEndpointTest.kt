package br.com.zup.edu

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CarrosEndpointTest(val repository: CarroRepository,val grpcClient:CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub){

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `deve adicionar um novo carro`() {
        //cenario

        //acao
        val response = grpcClient.adicionar(
            CarrosGrpcRequest
                .newBuilder()
                .setModelo("Gol")
                .setPlaca("HPX-1234")
                .build()
        )
        //validacao
        with(response) {
            assertNotNull(id)
            assertTrue(repository.existsById(id))
        }
    }

    @Test
    fun `nao deve adicionar novo carro quando carro com placa ja existente`(){
        //cenario
        val existente = repository.save(Carro(modelo = "Palio",placa = "OIP-9876"))

        //acao
        val erro = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                        CarrosGrpcRequest.newBuilder()
                            .setModelo("Ferrari")
                            .setPlaca(existente.placa)
                            .build()
            )
        }
        //validacao
        with(erro){
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("carro com placa existente", status.description)
        }
    }

    @Test
    fun `nao deve adicionar novo carro quando dados de entrada forem invalidados`(){
        //cenario

        //acao
        val erro = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                CarrosGrpcRequest.newBuilder()
                    .setModelo("")
                    .setPlaca("")
                    .build()
            )
        }
        //validacao
        with(erro){
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("dados de entrada invalidos", status.description)
        }
    }


    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub? {
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}
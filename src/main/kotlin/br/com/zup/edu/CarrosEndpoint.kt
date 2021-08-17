package br.com.zup.edu

import com.google.rpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.data.jpa.repository.JpaRepository
import jdk.jshell.Snippet
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CarrosEndpoint(@Inject val repository: CarroRepository): CarrosGrpcServiceGrpc.CarrosGrpcServiceImplBase() {

    override fun adicionar(request: CarrosGrpcRequest, responseObserver: StreamObserver<CarroResponse>) {

        if(repository.existsByPlaca(request.placa)){
            responseObserver.onError(io.grpc.Status.ALREADY_EXISTS
                .withDescription("carro com placa existente")
                .asRuntimeException())
            return
        }

        val carro = Carro(
            modelo = request.modelo,
            placa = request.placa
        )

        try{
            repository.save(carro)
        }catch (e: ConstraintViolationException){
            responseObserver.onError(io.grpc.Status.ALREADY_EXISTS
                .withDescription("dados de entrada invalidos")
                .asRuntimeException())
            return
        }


        responseObserver.onNext(CarroResponse.newBuilder().setId(carro.id!!).build())
        responseObserver.onCompleted()
    }

}
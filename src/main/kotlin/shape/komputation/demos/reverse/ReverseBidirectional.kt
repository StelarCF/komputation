package shape.komputation.demos.reverse

import shape.komputation.cpu.forward.units.simpleRecurrentUnit
import shape.komputation.functions.activation.ActivationFunction
import shape.komputation.initialization.gaussianInitialization
import shape.komputation.initialization.identityInitialization
import shape.komputation.initialization.zeroInitialization
import shape.komputation.layers.entry.inputLayer
import shape.komputation.layers.forward.concatenation
import shape.komputation.layers.forward.decoder.singleInputDecoder
import shape.komputation.layers.forward.encoder.singleOutputEncoder
import shape.komputation.loss.logisticLoss
import shape.komputation.networks.Network
import shape.komputation.networks.printLoss
import shape.komputation.optimization.stochasticGradientDescent
import java.util.*

fun main(args: Array<String>) {

    val random = Random(1)
    val seriesLength = 6
    val numberCategories = 10
    val numberExamples = 10_000
    val hiddenDimension = 30
    val numberIterations = 100
    val batchSize = 1

    val inputs = ReverseData.generateInputs(random, numberExamples, seriesLength, numberCategories)
    val targets = ReverseData.generateTargets(inputs, seriesLength, numberCategories)

    val identityInitialization = identityInitialization()
    val gaussianInitialization = gaussianInitialization(random, 0.0, 0.001)
    val zeroInitialization = zeroInitialization()

    val optimizationStrategy = stochasticGradientDescent(0.001)

    val forwardEncoderUnit = simpleRecurrentUnit(
        seriesLength,
        hiddenDimension,
        numberCategories,
        gaussianInitialization,
        identityInitialization,
        zeroInitialization,
        ActivationFunction.ReLU,
        optimizationStrategy
    )

    val backwardEncoderUnit = simpleRecurrentUnit(
        seriesLength,
        hiddenDimension,
        numberCategories,
        gaussianInitialization,
        identityInitialization,
        zeroInitialization,
        ActivationFunction.ReLU,
        optimizationStrategy
    )

    val decoderUnit = simpleRecurrentUnit(
        seriesLength,
        2 * hiddenDimension,
        numberCategories,
        identityInitialization,
        gaussianInitialization,
        zeroInitialization,
        ActivationFunction.ReLU,
        optimizationStrategy
    )

    val network = Network(
        inputLayer(),
        concatenation(
            singleOutputEncoder(forwardEncoderUnit, seriesLength, numberCategories, hiddenDimension, false),
            singleOutputEncoder(backwardEncoderUnit, seriesLength, numberCategories, hiddenDimension, true)
        ),
        singleInputDecoder(seriesLength, 2 * hiddenDimension, numberCategories, decoderUnit, gaussianInitialization, null, ActivationFunction.Softmax, optimizationStrategy)
    )

    network.train(
        inputs,
        targets,
        logisticLoss(),
        numberIterations,
        batchSize,
        printLoss
    )

}

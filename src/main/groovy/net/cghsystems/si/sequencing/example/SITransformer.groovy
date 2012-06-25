package net.cghsystems.si.sequencing.example



class SITransformer  {

    @org.springframework.integration.annotation.ServiceActivator
    public OutputModelObject transform(input) {
        return new OutputModelObject(name: input.name, sequence: input.sequence)
    }
}

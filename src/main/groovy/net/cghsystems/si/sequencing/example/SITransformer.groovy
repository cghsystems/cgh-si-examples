package net.cghsystems.si.sequencing.example



class SITransformer  {

    public OutputModelObject transform(input) {
        return new OutputModelObject(name: input.name, sequence: input.sequence)
    }
}

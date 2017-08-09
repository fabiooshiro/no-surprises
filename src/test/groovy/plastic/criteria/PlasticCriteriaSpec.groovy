package plastic.criteria

import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static plastic.criteria.PlasticCriteria.mockCriteria

@Mock([Artist, Portrait, City])
@TestFor(SaintPeter)
class PlasticCriteriaSpec extends CriteriaDocSpec {

    def saintPeter


    def setupSpec() {
        mockCriteria([Artist, Portrait])

    }

    def setup() {
        saintPeter = PlasticCriteria._SaintPeter
    }

    def cleanup() {
    }

    //next release 1.3
    def "test why not in result"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def soleil = new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()

        when:
        def results = Portrait.withCriteria{
            eq('name', 'not exists')
        }

        then:
        saintPeter.didYouSay("    eq('name', 'not exists') == false")
    }

    def "test SaintPeter should say 'Hey the list is empty'"(){
        when:
        def results = Portrait.withCriteria{
            eq('name', 'not exists')
        }
        then:
        saintPeter.didYouSay('Hey the Portrait.list() is empty!')

    }

}

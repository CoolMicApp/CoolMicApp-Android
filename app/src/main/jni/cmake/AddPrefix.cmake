#source https://github.com/juselius/vasp-cmake/blob/master/cmake/AddPrefix.cmake

# Prefix a variable or a list with a string.
#
# set (foo a b c)
# add_prefix(bar ../ "${foo}")
#
# jonas.juselius@uit.no 2013
#


function (add_prefix var pfix lst)
    foreach(i ${lst})
        set (f ${f} ${pfix}/${i})
    endforeach()
    set (${var} ${f} PARENT_SCOPE)
endfunction()
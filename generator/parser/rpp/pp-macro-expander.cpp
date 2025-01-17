
#include "rpp/pp-macro-expander.h"

std::string const *rpp::pp_macro_expander::resolve_formal(pp_fast_string const *name) {
    assert(name != nullptr);

    if (! frame)
        return nullptr;

    assert(frame->expanding_macro != nullptr);

    std::vector<pp_fast_string const *> const formals = frame->expanding_macro->formals;
    for (std::size_t index = 0; index < formals.size(); ++index) {
        pp_fast_string const *formal = formals[index];

        if (*formal != *name)
            continue;

        else if (frame->actuals && index < frame->actuals->size())
            return &(*frame->actuals)[index];

        else{
            env.log(QString("Macro %1 expects %2 arguments. Given: %3").arg(frame->expanding_macro->name->begin()).arg(formals.size()).arg(frame->actuals ? frame->actuals->size() : 0).toStdString());
            exit(-1);
            // internal error?
        }
    }

    return nullptr;
}

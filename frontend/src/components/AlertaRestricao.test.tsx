import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import AlertaRestricao from './AlertaRestricao'

describe('AlertaRestricao', () => {
  it('não renderiza sem conflito', () => {
    const { container } = render(<AlertaRestricao nomesAlergenos={[]} />)
    expect(container).toBeEmptyDOMElement()
  })

  it('avisa quando o item conflita com a restrição', () => {
    render(<AlertaRestricao nomesAlergenos={['Glúten', 'Trigo']} />)
    expect(screen.getByRole('status')).toHaveTextContent('Glúten')
    expect(screen.getByRole('status')).toHaveTextContent('Trigo')
  })
})
